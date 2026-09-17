package app.template.patches.andropods.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * PairIP's static startup entry point. The injected PairIP Application calls this
 * before the app UI is created and displays a Google Play ownership screen for
 * re-signed APKs. The SDK class and method names are intentionally not obfuscated.
 */
object AndroPodsPairIpCheckLicenseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
)

/**
 * Targets the purchase result handler Y(List<Purchase>)V of the PreferencesFragment.
 *
 * Called when queryPurchasesAsync() completes. Iterates the purchase list, extracts
 * productIds from the Purchase JSON, and sets the premium boolean if "pro" is found.
 *
 * Stable anchors (in smali instruction order):
 *   1. string "productIds"           — JSONObject key for the productId array
 *   2. JSONObject.optJSONArray()     — reads the array immediately after has()
 *   3. string "pro"                  — the product ID literal
 *   4. ArrayList.contains()          — the membership test that gates iput-boolean m0
 */
object AndroPodsPurchaseResultFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/util/List;"),
    filters = listOf(
        string("productIds"),
        methodCall(
            definingClass = "Lorg/json/JSONObject;",
            name = "optJSONArray",
        ),
        string("pro"),
        methodCall(
            definingClass = "Ljava/util/ArrayList;",
            name = "contains",
        ),
    ),
)
