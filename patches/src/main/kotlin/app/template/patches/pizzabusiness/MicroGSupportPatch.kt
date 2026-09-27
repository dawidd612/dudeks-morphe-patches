package app.template.patches.pizzabusiness

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.replaceBody
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import org.w3c.dom.Element

internal const val MICROG = "app.revanced.android.gms"
private const val GAME = "com.tapblaze.pizzabusiness"
// SHA-1 of the verified APK signing certificate, not of the APK contents.
private const val SIGNER = "828d99f1d85e52eb473af06d690f84ee72904330"
private const val GAME_ACTIVITY = "Lcom/tapblaze/pizzabusiness/BaseAppActivity;"
private const val GMS_CLIENT = "Lcom/google/android/gms/common/internal/f;"
private val gamesClients = mapOf(
    "Lcom/google/android/gms/internal/games_v2/zzp;" to "games.internal.connect.service.START",
    "Lld/d;" to "games.service.START",
)

private val pizzaMicroGResources = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            requirePizza(manifest.getAttribute("package") == GAME &&
                manifest.getAttribute("android:versionCode") == "2277", "MicroG manifest target changed")
            val application = document.getElementsByTagName("application").item(0) as Element
            val metadata = mapOf(
                "$MICROG.SPOOFED_PACKAGE_NAME" to GAME,
                "$MICROG.SPOOFED_PACKAGE_SIGNATURE" to SIGNER,
                "app.revanced.MICROG_PACKAGE_NAME" to MICROG,
            )
            val existing = application.getElementsByTagName("meta-data")
            requirePizza((0 until existing.length).none {
                (existing.item(it) as Element).getAttribute("android:name") in metadata
            }, "MicroG metadata already present; use a clean APK")
            metadata.forEach { (name, value) ->
                application.appendChild(document.createElement("meta-data").apply {
                    setAttribute("android:name", name)
                    setAttribute("android:value", value)
                })
            }
            val queries = (document.getElementsByTagName("queries").item(0) as? Element)
                ?: document.createElement("queries").also { manifest.appendChild(it) }
            val packages = queries.getElementsByTagName("package")
            if ((0 until packages.length).none {
                (packages.item(it) as Element).getAttribute("android:name") == MICROG
            }) queries.appendChild(document.createElement("package").apply {
                setAttribute("android:name", MICROG)
            })
        }
    }
}

/** Redirect only the two Games clients; keep Binder descriptors and Bundle keys. */
@Suppress("unused")
val pizzaMicroGSupportPatch = bytecodePatch(
    name = "Google Play Games via MicroG-RE",
    description = "Routes Google Play Games sign-in and player/server authorization through MicroG-RE 7.1.0+. Requires app.revanced.android.gms. Experimental: cloud save and restore need device verification.",
    default = true,
) {
    compatibleWith(Compatibility(
        name = "Good Pizza, Great Pizza",
        packageName = GAME,
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xB47454,
        targets = listOf(AppTarget(version = "5.57.3", versionCode = 2277, isExperimental = true)),
    ))
    dependsOn(pizzaStartupSupport, pizzaMicroGResources)
    execute {
        // Authentication still comes from the remote service. These overrides
        // select its transport; none returns an authenticated player or token.
        val overrides = mapOf(
            "getStartServicePackage" to (STRING to "const-string v0, \"$MICROG\"\nreturn-object v0"),
            "getUseDynamicLookup" to ("Z" to "const/4 v0, 0x0\nreturn v0"),
            "requiresGooglePlayServices" to ("Z" to "const/4 v0, 0x0\nreturn v0"),
        )
        overrides.forEach { (name, shape) ->
            val inherited = pizzaMethod(GMS_CLIENT, name, emptyList(), shape.first, static = false)
            requirePizza(!AccessFlags.FINAL.isSet(inherited.accessFlags), "$name cannot be overridden")
        }
        val actions = gamesClients.map { (owner, action) ->
            requirePizza(classDefBy(owner).superclass == "Lcom/google/android/gms/common/internal/i;" &&
                classDefBy(owner).methods.none { it.name in overrides }, "Games client inheritance changed")
            pizzaMethod(owner, "getStartServiceAction", emptyList(), STRING, static = false).also {
                requirePizza(it.hasString("com.google.android.gms.$action"), "Games service action changed")
            }
        }
        val installed = pizzaMethod(GAME_ACTIVITY, "isGooglePlayGamesInstalled", emptyList(), "Z")
        requirePizza(installed.hasString("com.google.android.play.games") &&
            installed.calls("Landroid/content/pm/PackageManager;", "getApplicationInfo"), "Games presence check changed")

        actions.forEachIndexed { index, method ->
            val (owner, action) = gamesClients.entries.elementAt(index)
            method.replaceBody("const-string v0, \"$MICROG.$action\"\nreturn-object v0")
            overrides.forEach { (name, shape) ->
                val added = MutableMethod(ImmutableMethod(owner, name, emptyList(), shape.first,
                    AccessFlags.PUBLIC.value, emptySet(), emptySet(),
                    ImmutableMethodImplementation(2, emptyList(), emptyList(), emptyList())))
                added.addInstructions(0, shape.second)
                mutableClassDefBy(owner).methods.add(added)
            }
        }
        // Preserve the actual package-manager check and its failure handling.
        // MicroG hosts Games itself; the separate Play Games app is unnecessary.
        val instructions = installed.implementation!!.instructions.toList()
        instructions.forEachIndexed { index, instruction ->
            val reference = (instruction as? ReferenceInstruction)?.reference as? StringReference
            if (reference?.string == "com.google.android.play.games") {
                val register = (instruction as OneRegisterInstruction).registerA
                installed.replaceInstruction(index,
                    BuilderInstruction21c(Opcode.CONST_STRING, register, ImmutableStringReference(MICROG)))
            }
        }
    }
}
