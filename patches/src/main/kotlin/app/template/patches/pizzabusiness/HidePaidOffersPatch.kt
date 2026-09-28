package app.template.patches.pizzabusiness

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.replaceBody
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import java.security.MessageDigest

private val pizzaFundsStore = rawResourcePatch {
    dependsOn(pizzaNativeBootstrap)
    execute {
        val file = get("lib/arm64-v8a/libcocos2dcpp.so")
        val bytes = file.readBytes()
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it) }
        requirePizza(hash == "18007200015b853ef0ca954ec588ee4cb778690e6c6bd5be9d6ef7a2df017af0",
            "paid-offer visibility requires the restored 5.57.3 ARM64 library")
        // StoreLayer's existing view filter: FUNDS (0x40). It selects the
        // in-game gem-to-funds exchanges instead of the ALL/paid-store view.
        // No balance, ownership, receipt or purchase-success code changes.
        val offset = 0x182f25c
        requirePizza(bytes.copyOfRange(offset, offset + 4).contentEquals(
            byteArrayOf(0xf9.toByte(), 0x03, 0x02, 0x2a)), "store view argument changed")
        byteArrayOf(0x19, 0x08, 0x80.toByte(), 0x52).copyInto(bytes, offset) // mov w25,#0x40
        // The starter promotion has a separate visibility predicate, shared
        // by its home-screen icon and popup. Hide the offer without marking it
        // purchased or changing any inventory/subscription entitlement.
        val starter = 0x1651858
        requirePizza(bytes.copyOfRange(starter, starter + 8).contentEquals(
            byteArrayOf(0xff.toByte(), 0x43, 0x01, 0xd1.toByte(), 0xfd.toByte(), 0x7b, 0x02, 0xa9.toByte())),
            "starter promotion visibility predicate changed")
        byteArrayOf(0x00, 0x00, 0x80.toByte(), 0x52, 0xc0.toByte(), 0x03, 0x5f, 0xd6.toByte())
            .copyInto(bytes, starter) // mov w0,#0; ret
        file.writeBytes(bytes)
    }
}

@Suppress("unused")
val pizzaHidePaidOffersPatch = bytecodePatch(
    name = "Hide paid offers",
    description = "Hides real-money storefront sections and cancels Google Play checkout. Keeps in-game currency exchanges and existing purchase processing. Requires ARM64 Pizza 5.57.3.",
    default = true,
) {
    compatibleWith(Compatibility(
        name = "Good Pizza, Great Pizza",
        packageName = "com.tapblaze.pizzabusiness",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xB47454,
        targets = listOf(AppTarget(version = "5.57.3", versionCode = 2277, isExperimental = true)),
    ))
    dependsOn(pizzaStartupSupport, pizzaFundsStore)
    execute {
        val owner = "Lcom/tapblaze/pizzabusiness/PurchasesManager;"
        val canceled = "Lcom/tapblaze/pizzabusiness/PurchasesManager\$1;"
        val activity = "Lcom/tapblaze/pizzabusiness/BaseAppActivity;"
        val entry = pizzaMethod(owner, "purchase", listOf(STRING), "V")
        val callback = pizzaMethod(canceled, "run", emptyList(), "V", static = false)
        val cancellationBridge = pizzaMethod(owner, "access\$000", emptyList(), "V")
        requirePizza(entry.calls(owner, "purchaseInternal") &&
            callback.calls(owner, "access\$000") && cancellationBridge.calls(owner, "reportPurchaseFailed"),
            "purchase/cancellation entry changed")
        // Reuse the game's real cancellation callback on its GL queue. This
        // releases its pending UI and never opens Billing or grants a purchase.
        val replacement = MutableMethod(ImmutableMethod(entry.definingClass, entry.name,
            entry.parameters, entry.returnType, entry.accessFlags, entry.annotations,
            entry.hiddenApiRestrictions, ImmutableMethodImplementation(3, emptyList(), emptyList(), emptyList())))
        replacement.replaceBody("""
            invoke-static {}, $owner->getInstance()$owner
            move-result-object v0
            new-instance v1, $canceled
            invoke-direct {v1, v0}, $canceled-><init>($owner)V
            invoke-static {}, $activity->getInstance()$activity
            move-result-object v0
            invoke-virtual {v0, v1}, $activity->runOnGLThread($RUNNABLE)V
            return-void
        """.trimIndent())
        mutableClassDefBy(owner).methods.apply { remove(entry); add(replacement) }
    }
}
