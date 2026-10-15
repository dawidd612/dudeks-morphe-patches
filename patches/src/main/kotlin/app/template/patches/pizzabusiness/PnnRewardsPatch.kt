package app.template.patches.pizzabusiness

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.rawResourcePatch
import java.util.Base64

@Suppress("unused")
val pizzaPnnRewardsPatch = rawResourcePatch(
    name = "1000 PNN sponsor rewards",
    description = "Zwieksza sekwencje reklam sponsora na poczatku dnia z 2 do 1000. Zachowuje zwykle nagrody i mozliwosc pominiecia. Wspolpracuje ze Skip rewarded ads. Pizza 5.58.0 ARM64; eksperymentalne.",
    default = true,
) {
    compatibleWith(Compatibility(
        name = "Good Pizza, Great Pizza", packageName = "com.tapblaze.pizzabusiness",
        apkFileType = ApkFileType.APKM, appIconColor = 0xB47454,
        targets = PizzaProfiles.profiles.filter { it.version == "5.58.0" }.map { it.target },
    ))
    dependsOn(pizzaStartupSupport)
    // Run after the optional funds-store delta has consumed its exact input hash.
    finalize {
        val profile = PizzaProfiles.forPackage(packageMetadata)
        require(profile.version == "5.58.0")
        val payload = profile.resource("pnn-1000-arm64.b64").bufferedReader().use {
            Base64.getDecoder().decode(it.readText().trim())
        }
        val library = get("lib/arm64-v8a/libcocos2dcpp.so")
        library.writeBytes(PizzaPnnNative.apply(library.readBytes(), payload))
    }
}
