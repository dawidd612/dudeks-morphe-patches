package app.template.patches.pizzabusiness

import app.morphe.patcher.PackageMetadata
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.SupportedAbi
import java.io.InputStream
import java.util.Properties

/** One profile owns compatibility, recovered initialization and reviewed native edits. */
internal class PizzaProfile(val directory: String) {
    private val properties = Properties().apply { resource("profile.properties").use { load(it) } }
    val version: String = properties.getProperty("version")
    val versionCode: String = properties.getProperty("versionCode")
    val startupProgram: String = properties.getProperty("startupProgram")
    val target = AppTarget(version = version,
        versionCodes = mapOf(SupportedAbi.ARM64_V8A to versionCode.toInt()),
        isExperimental = properties.getProperty("experimental").toBooleanStrict(),
        description = "Requires complete ARM64 APKM/XAPK initialization resources.")

    fun resource(name: String): InputStream = PizzaProfiles::class.java
        .getResourceAsStream("/pizzabusiness/$directory/$name")
        ?: throw PatchException("Pizza $version bootstrap resource missing: $name")
}

internal object PizzaProfiles {
    val profiles: List<PizzaProfile> = javaClass.getResourceAsStream("/pizzabusiness/profiles.txt")!!
        .bufferedReader().useLines { lines ->
            lines.filter { it.isNotBlank() && !it.startsWith("#") }.map(::PizzaProfile).toList()
        }
    val compatibility = Compatibility(
        name = "Good Pizza, Great Pizza", packageName = "com.tapblaze.pizzabusiness",
        apkFileType = ApkFileType.APKM, appIconColor = 0xB47454,
        targets = profiles.map { it.target },
    )

    fun forPackage(metadata: PackageMetadata): PizzaProfile = profiles.singleOrNull {
        metadata.packageName == "com.tapblaze.pizzabusiness" &&
            metadata.versionName == it.version && metadata.versionCode == it.versionCode
    } ?: throw PatchException("Pizza ${metadata.versionName} (${metadata.versionCode}) has no verified bootstrap profile. " +
        "Use a complete clean APKM/XAPK for a supported target; a new native build needs initialization capture.")
}
