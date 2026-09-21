package app.template.patches.tiktok.scroll

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private fun checkShape(value: Boolean, message: String) {
    if (!value) throw PatchException("Keep TikTok DM scroll position: $message")
}

@Suppress("unused")
val keepTikTokDmScrollPositionPatch = bytecodePatch(
    name = "Keep TikTok DM scroll position",
    description = "Keeps the conversation in place after replying to an older message or video. Compatible with kveld9 patches. / Zachowuje pozycję rozmowy po odpowiedzi na starszą wiadomość lub film. Zgodny z patchami kveld9.",
    default = true,
) {
    compatibleWith(Compatibility(
        name = "TikTok",
        packageName = "com.zhiliaoapp.musically",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFE2C55,
        targets = listOf(AppTarget(version = "47.0.3")),
    ))

    execute {
        val decision = ScrollDecisionFingerprint.method
        val sendEvent = SendMessageEventFingerprint.classDef
        val addEvent = AddMessageEventFingerprint.classDef
        val ownCheck = OwnMessageCheckFingerprint.method
        val sender = ownCheck.calls().single { it.name == "getSender" && it.returnType == "J" }
        val isSelf = ownCheck.calls().single { it.parameterTypes == listOf("J") && it.returnType == "Z" }
        val message = classDefBy(sender.definingClass)
        val reference = message.methods.single {
            it.name == "getReferenceInfo" && it.parameterTypes.isEmpty() &&
                it.returnType == "Lcom/bytedance/im/core/proto/ReferenceInfo;"
        }
        val sendMessage = sendEvent.fields.single { it.type == message.type }
        val addMessage = addEvent.fields.single { it.type == message.type }
        val atLatest = ScrollDecisionFingerprint.classDef.methods.single {
            it.returnType == "Z" && it.parameterTypes.isEmpty() &&
                it.calls().any { call -> call.name == "findFirstVisibleItemPosition" } &&
                it.calls().any { call ->
                    call.definingClass == "Lcom/ss/android/ugc/aweme/im/messagelist/api/ability/LoadMoreAbility;"
                }
        }
        val eventTypes = decision.implementation!!.instructions.filter { it.opcode == Opcode.INSTANCE_OF }
            .map { ((it as ReferenceInstruction).reference as TypeReference).type }.toSet()
        checkShape(sendEvent.type in eventTypes && addEvent.type in eventTypes,
            "scroll decision no longer distinguishes send/add events")
        checkShape(decision.accessFlags and AccessFlags.STATIC.value == 0 &&
            decision.implementation!!.registerCount - decision.parameterTypes.size - 1 >= 2,
            "expected two spare local registers in an instance method")
        checkShape(atLatest.accessFlags and AccessFlags.PUBLIC.value != 0,
            "native latest-message check is not public")

        // Only decline the extra scroll request. The caller still submits the
        // dataset, invokes message protocols and updates the sent-message status.
        // All non-reply events execute the complete original decision unchanged.
        decision.addInstructionsWithLabels(
            0,
            """
                instance-of v0, p1, ${sendEvent.type}
                if-nez v0, :dudeks_sent_reply
                instance-of v0, p1, ${addEvent.type}
                if-eqz v0, :dudeks_original
                move-object v0, p1
                check-cast v0, ${addEvent.type}
                iget-object v0, v0, $addMessage
                goto :dudeks_check_reply
                :dudeks_sent_reply
                move-object v0, p1
                check-cast v0, ${sendEvent.type}
                iget-object v0, v0, $sendMessage
                :dudeks_check_reply
                if-eqz v0, :dudeks_original
                invoke-virtual {v0}, $reference
                move-result-object v1
                if-eqz v1, :dudeks_original
                invoke-virtual {v0}, $sender
                move-result-wide v0
                invoke-static {v0, v1}, $isSelf
                move-result v0
                if-eqz v0, :dudeks_original
                invoke-virtual {p0}, $atLatest
                move-result v0
                if-nez v0, :dudeks_original
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
            ExternalLabel("dudeks_original", decision.getInstruction(0)),
        )
    }
}
