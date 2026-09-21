package app.template.patches.tiktok.scroll

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

internal fun Method.calls() = implementation?.instructions?.mapNotNull {
    (it as? ReferenceInstruction)?.reference as? MethodReference
}.orEmpty()

internal object SendMessageEventFingerprint : Fingerprint(
    strings = listOf("OnSendMessageEvent"),
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
)

internal object AddMessageEventFingerprint : Fingerprint(
    strings = listOf("OnAddMessageEvent"),
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
)

// IMScrollToBottomAbilityImpl: decides whether a dataset submission also queues
// a scroll. Incoming messages, navigation and initial loading share this method.
internal object ScrollDecisionFingerprint : Fingerprint(
    returnType = "Z",
    custom = { method, _ ->
        method.parameterTypes.size == 4 &&
            method.parameterTypes[1] == "Ljava/util/List;" &&
            method.parameterTypes[3] == "Z" &&
            method.calls().any {
                it.definingClass == "Lcom/ss/android/ugc/aweme/im/sdk/chat/data/model/FakeMessageKt;" &&
                    it.name == "isFakeMessage"
            } && method.calls().any { it.name == "getMsgStatus" }
    },
)

// The native double-tap helper checks the message sender against the signed-in
// account. Discover that account check instead of copying an obfuscated name.
internal object OwnMessageCheckFingerprint : Fingerprint(
    returnType = "Z",
    custom = { method, classDef ->
        method.parameterTypes.size == 1 && method.calls().any { it.name == "getSender" } &&
            method.calls().count { it.parameterTypes == listOf("J") && it.returnType == "Z" } == 1 &&
            classDef.methods.any { sibling ->
                sibling.calls().any { it.name == "getScene" } &&
                    sibling.implementation?.instructions?.any {
                        ((it as? ReferenceInstruction)?.reference as? StringReference)?.string == "double_tap"
                    } == true
            }
    },
)
