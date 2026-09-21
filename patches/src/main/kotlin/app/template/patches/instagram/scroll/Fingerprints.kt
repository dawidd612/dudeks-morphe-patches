package app.template.patches.instagram.scroll

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal fun Method.calls() = implementation?.instructions?.mapNotNull {
    (it as? ReferenceInstruction)?.reference as? MethodReference
}.orEmpty()

internal object DirectTextSendFingerprint : Fingerprint(
    strings = listOf("DirectThreadFragment.sendTextMessage", "DirectSendHelperImpl.sendSideChatContextualQuery"),
    returnType = "Z",
)

// Structural checks in the patch additionally verify the callback interface from
// DirectTextSendFingerprint, its reply argument and the delayed scroll runnable.
internal object DirectSendCompletedFingerprint : Fingerprint(
    strings = listOf("ig_direct", "wa_business_number_share_business_share", "page_id", "target_user_primary_name"),
    returnType = "V",
    custom = { method, _ ->
        method.parameterTypes.size == 4 && method.parameterTypes.last() == "I" &&
            method.calls().any {
                it.definingClass == "Landroid/view/View;" && it.name == "postDelayed" &&
                    it.parameterTypes == listOf("Ljava/lang/Runnable;", "J")
            }
    },
)

// The layout-time bottom-edge nudge is independent of the send-to-latest runnable.
internal object DirectViewportLayoutFingerprint : Fingerprint(
    strings = listOf(
        "DirectMessageListLinearLayoutManager.onLayoutChildren",
        "DirectThreadScrollBottomIntoViewportLayoutHelper.afterLayoutChildren",
    ),
    returnType = "V",
    custom = { method, _ -> method.parameterTypes.size == 2 },
)

internal const val PIKO_SETTINGS = "Lapp/morphe/extension/instagram/settings"
internal const val SCREEN_BUILDER = "$PIKO_SETTINGS/preference/ScreenBuilder;"
internal const val HELPER = "$PIKO_SETTINGS/preference/Helper;"
internal const val HOOK = "Lpl/dudek/extension/instagram/KeepDmScrollPosition;"
