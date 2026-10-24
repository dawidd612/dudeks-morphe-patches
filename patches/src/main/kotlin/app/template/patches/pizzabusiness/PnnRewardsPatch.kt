package app.template.patches.pizzabusiness

import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import java.io.DataInputStream
import java.security.MessageDigest
import java.util.zip.GZIPInputStream

@Suppress("unused")
val pizzaPnnRewardsPatch = rawResourcePatch(
    name = "1000 PNN sponsor rewards",
    description = "Extends the day-start sponsor sequence to 1000 offers, with 10 gems per completed reward. Preserves skipping and the final PNN2 close flow. Works with Skip rewarded ads.",
    default = true,
) {
    compatibleWith(Compatibility(
        name = "Good Pizza, Great Pizza", packageName = "com.tapblaze.pizzabusiness",
        apkFileType = PizzaProfiles.compatibility.apkFileType,
        appIconColor = 0xB47454,
        targets = PizzaProfiles.profiles.filter { it.pnnDeltas.isNotEmpty() }.map { it.target },
    ))
    dependsOn(pizzaStartupSupport)
    // The optional funds-store transform must consume its own input before PNN.
    finalize {
        val profile = PizzaProfiles.forPackage(packageMetadata)
        val library = get("lib/arm64-v8a/libcocos2dcpp.so")
        val original = library.readBytes()
        val hash = MessageDigest.getInstance("SHA-256").digest(original)
        val delta = profile.pnnDeltas.singleOrNull { name ->
            profile.resource(name).use { stream ->
                DataInputStream(GZIPInputStream(stream)).use { input ->
                    input.readInt() // PZB2; restoreNative validates the complete envelope.
                    ByteArray(32).also(input::readFully).contentEquals(hash)
                }
            }
        } ?: throw PatchException("PNN sponsor rewards require this profile's exact restored ARM64 library.")
        val output = profile.resource(delta).use { PizzaBootstrap.restoreNative(original, it) }
        library.writeBytes(output)
    }
}
