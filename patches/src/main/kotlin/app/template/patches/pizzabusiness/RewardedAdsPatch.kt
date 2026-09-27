package app.template.patches.pizzabusiness

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.replaceBody
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

internal const val BASE = "Lcom/tapblaze/pizzabusiness/BaseIronSourceWrapper;"
internal const val BRIDGE = "Lcom/tapblaze/pizzabusiness/IronSourceWrapper;"
internal const val REQUEST = "Lcom/tapblaze/pizzabusiness/BaseIronSourceWrapper\$1;"
internal const val STRING = "Ljava/lang/String;"
internal const val RUNNABLE = "Ljava/lang/Runnable;"
internal const val ACTIVITY = "Lorg/cocos2dx/lib/Cocos2dxActivity;"
internal const val HELPER = "Lpl/dudek/extension/pizzabusiness/RewardedAds;"
private const val AD = "Lcom/unity3d/mediation/rewarded/LevelPlayRewardedAd;"

internal fun requirePizza(value: Boolean, message: String) {
    if (!value) throw PatchException("Skip rewarded ads: $message. Use clean Good Pizza, Great Pizza 5.57.3 (2277).")
}

internal fun Method.calls(owner: String, name: String): Boolean =
    implementation?.instructions?.any {
        val ref = (it as? ReferenceInstruction)?.reference as? MethodReference
        ref?.definingClass == owner && ref.name == name
    } == true

internal fun Method.hasString(value: String): Boolean = implementation?.instructions?.any {
    ((it as? ReferenceInstruction)?.reference as? StringReference)?.string == value
} == true

internal fun BytecodePatchContext.pizzaMethod(
    owner: String, name: String, parameters: List<String>, returns: String,
    static: Boolean = true,
): MutableMethod {
    val method = mutableClassDefBy(owner).methods.singleOrNull {
        it.name == name && it.parameterTypes == parameters && it.returnType == returns
    } ?: throw PatchException("Skip rewarded ads: missing $owner->$name signature")
    requirePizza(AccessFlags.STATIC.isSet(method.accessFlags) == static, "$name static/instance shape changed")
    return method
}

@Suppress("unused")
val skipRewardedAdsPatch = bytecodePatch(
    name = "Skip rewarded ads",
    description = "Completes the game's rewarded-video flow without playing an ad, keeping the requested placement and normal reward. Experimental; includes re-signed startup support and needs device testing.",
    default = true,
) {
    compatibleWith(Compatibility(
        name = "Good Pizza, Great Pizza",
        packageName = "com.tapblaze.pizzabusiness",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xB47454,
        targets = listOf(AppTarget(version = "5.57.3", versionCode = 2277, isExperimental = true)),
    ))
    dependsOn(pizzaStartupSupport)
    extendWith("extensions/pizzabusiness.mpe")

    execute {
        val show = pizzaMethod(BASE, "showRewardedVideo", listOf(STRING), "V")
        val ready = pizzaMethod(BASE, "isRewardedVideoReady", listOf(STRING), "Z")
        val initialize = pizzaMethod(BASE, "Initialize", listOf(STRING, STRING, STRING, "Z", "I"), "V")
        val run = pizzaMethod(REQUEST, "run", emptyList(), "V", static = false)
        val constructor = pizzaMethod(REQUEST, "<init>", listOf(STRING), "V", static = false)

        requirePizza(classDefBy(BRIDGE).superclass == BASE, "JNI bridge superclass changed")
        listOf("onInitialized", "onVideoReady", "onVideoStarted", "onVideoEnded", "onVideoWatched").forEach { name ->
            val callback = pizzaMethod(BRIDGE, name, if (name == "onVideoWatched") listOf(STRING) else emptyList(), "V")
            requirePizza(AccessFlags.NATIVE.isSet(callback.accessFlags) && callback.implementation == null,
                "$name is no longer the original native callback")
        }
        requirePizza(RUNNABLE in classDefBy(REQUEST).interfaces &&
            classDefBy(REQUEST).fields.count { it.name == "val\$placement" && it.type == STRING } == 1,
            "placement capture changed")
        requirePizza(constructor.implementation?.registerCount == 2 &&
            constructor.instructions.map { it.opcode } == listOf(Opcode.IPUT_OBJECT, Opcode.INVOKE_DIRECT, Opcode.RETURN_VOID),
            "request constructor changed")
        requirePizza(run.implementation?.registerCount == 4 && run.implementation!!.tryBlocks.isEmpty() &&
            run.calls(AD, "showAd") && run.calls(AD, "isAdReady"), "reward request body changed or already patched")
        requirePizza(ready.implementation?.registerCount == 1 && ready.implementation!!.tryBlocks.isEmpty() &&
            ready.calls(AD, "isAdReady"), "readiness body changed or already patched")
        requirePizza(initialize.hasString("[BaseIronSourceWrapper] TD cache in Initialize: ") &&
            initialize.calls("Landroid/app/Activity;", "runOnUiThread") && !initialize.calls(HELPER, "initialize"),
            "initialization changed or already patched")
        requirePizza(pizzaMethod(ACTIVITY, "runOnGLThread", listOf(RUNNABLE), "V", static = false)
            .calls("Lorg/cocos2dx/lib/Cocos2dxGLSurfaceView;", "queueEvent"), "game queue changed")
        pizzaMethod("Lorg/cocos2dx/lib/Cocos2dxHelper;", "runOnGLThread", listOf(RUNNABLE), "V")

        // This Runnable is private to the rewarded gateway, not interstitials.
        var constructorCalls = 0
        classDefForEach { cls ->
            cls.methods.forEach { method ->
                if (method.calls(REQUEST, "<init>")) constructorCalls++
            }
        }
        requirePizza(constructorCalls == 1 && show.calls(REQUEST, "<init>"), "request is shared with another flow")
        val code = show.instructions.toList()
        requirePizza(code.map { it.opcode } == listOf(Opcode.INVOKE_STATIC, Opcode.MOVE_RESULT_OBJECT,
            Opcode.NEW_INSTANCE, Opcode.INVOKE_DIRECT, Opcode.INVOKE_VIRTUAL, Opcode.RETURN_VOID) &&
            show.calls("Landroid/app/Activity;", "runOnUiThread"), "show gateway changed")
        val post = code[4] as FiveRegisterInstruction
        requirePizza(post.registerCount == 2, "dispatch arguments changed")

        // All guards finish before these related bytecode changes are made.
        initialize.addInstructions(0, "invoke-static {}, $HELPER->initialize()V")
        ready.replaceBody("const/4 p0, 0x1\nreturn p0")
        show.replaceInstruction(4,
            "invoke-virtual {v${post.registerC}, v${post.registerD}}, $ACTIVITY->runOnGLThread($RUNNABLE)V")
        // The same captured placement is passed to the native reward handler.
        // Its scheduler preserves insertion order for these distinct timer keys.
        // No LevelPlay display, impression, revenue or postback is synthesized.
        run.replaceBody("""
            invoke-static {}, $BRIDGE->onVideoStarted()V
            iget-object v0, p0, $REQUEST->val${'$'}placement:$STRING
            if-nez v0, :placement_ready
            const-string v0, "DefaultRewardedVideo"
            :placement_ready
            invoke-static {v0}, $BRIDGE->onVideoWatched($STRING)V
            invoke-static {}, $BRIDGE->onVideoEnded()V
            invoke-static {}, $BRIDGE->onVideoReady()V
            return-void
        """.trimIndent())
    }
}
