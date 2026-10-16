package app.template.patches.coffeebusiness

import app.morphe.patcher.PackageMetadata
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.SupportedAbi
import java.io.InputStream
import java.util.Properties

/** One profile owns compatibility, recovered initialization and reviewed native edits. */
internal class CoffeeProfile(val directory: String) {
    private val properties = Properties().apply { resource("profile.properties").use { load(it) } }
    val version: String = properties.getProperty("version")
    val versionCode: String = properties.getProperty("versionCode")
    val startupProgram: String = properties.getProperty("startupProgram")
    val gamesConnect: String = properties.getProperty("gamesConnect")
    val gamesService: String = properties.getProperty("gamesService")
    val gamesParent: String = properties.getProperty("gamesParent")
    val resolutionOwner: String = properties.getProperty("resolutionOwner")
    val retryOwner: String = properties.getProperty("retryOwner")
    val target = AppTarget(version = version,
        versionCodes = mapOf(SupportedAbi.ARM64_V8A to versionCode.toInt()),
        isExperimental = properties.getProperty("experimental").toBooleanStrict(),
        description = "Requires complete ARM64 APKM/XAPK initialization resources.")

    fun resource(name: String): InputStream = CoffeeProfiles::class.java
        .getResourceAsStream("/coffeebusiness/$directory/$name")
        ?: throw PatchException("Coffee $version bootstrap resource missing: $name")
}

internal object CoffeeProfiles {
    val profiles: List<CoffeeProfile> = javaClass.getResourceAsStream("/coffeebusiness/profiles.txt")!!
        .bufferedReader().useLines { lines ->
            lines.filter { it.isNotBlank() && !it.startsWith("#") }.map(::CoffeeProfile).toList()
        }
    val compatibility = Compatibility(
        name = "Good Coffee, Great Coffee", packageName = "com.tapblaze.coffeebusiness",
        apkFileType = ApkFileType.APKM, appIconColor = 0x98704C,
        targets = profiles.map { it.target },
    )

    fun forPackage(metadata: PackageMetadata): CoffeeProfile = profiles.singleOrNull {
        metadata.packageName == "com.tapblaze.coffeebusiness" &&
            metadata.versionName == it.version && metadata.versionCode == it.versionCode
    } ?: throw PatchException("Coffee ${metadata.versionName} (${metadata.versionCode}) has no verified bootstrap profile. " +
        "Use a complete clean APKM/XAPK for a supported target; a new native build needs initialization capture.")
}
