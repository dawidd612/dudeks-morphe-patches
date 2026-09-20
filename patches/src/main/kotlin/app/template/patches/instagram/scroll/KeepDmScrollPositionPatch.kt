package app.template.patches.instagram.scroll

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.SupportedAbi.ARM64_V8A
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private fun checkShape(value: Boolean, message: String) {
    if (!value) throw PatchException("Keep DM scroll position: $message")
}

@Suppress("unused")
val keepDmScrollPositionPatch = bytecodePatch(
    name = "Keep DM scroll position",
    description = "Prevents Instagram Direct from jumping to the newest message after replying to an older message. Requires Piko Add settings in the same patching run.",
    default = false,
) {
    compatibleWith(Compatibility(
        name = "Instagram",
        packageName = "com.instagram.android",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xFC483C,
        targets = listOf(AppTarget(version = "439.0.0.37.89", versionCodes = mapOf(ARM64_V8A to 384510827))),
    ))
    extendWith("extensions/instagram.mpe")

    execute {
        val completed = DirectSendCompletedFingerprint.method
        val callback = DirectTextSendFingerprint.method.calls().filter {
            it.name == completed.name && it.returnType == "V" &&
                it.parameterTypes == completed.parameterTypes
        }.distinctBy { it.toString() }.single()
        checkShape(callback.definingClass in DirectSendCompletedFingerprint.classDef.interfaces,
            "the send-completed method no longer implements the sender's callback")

        val postIndex = completed.instructions.indexOfFirst {
            ((it as? ReferenceInstruction)?.reference as? MethodReference)?.let { ref ->
                ref.definingClass == "Landroid/view/View;" && ref.name == "postDelayed"
            } == true
        }
        checkShape(postIndex >= 2, "missing delayed send scroll")
        val post = completed.getInstruction<Instruction35c>(postIndex)
        val delay = completed.getInstruction<TwoRegisterInstruction>(postIndex - 1)
        checkShape(completed.getInstruction(postIndex - 1).opcode == Opcode.INT_TO_LONG,
            "unexpected delay conversion")
        val constructorInstruction = completed.getInstruction<Instruction35c>(postIndex - 2)
        val constructor = constructorInstruction.reference as MethodReference
        checkShape(constructorInstruction.opcode == Opcode.INVOKE_DIRECT && constructor.name == "<init>" &&
            constructor.parameterTypes.size == 1 && constructorInstruction.registerC == post.registerD,
            "unexpected delayed scroll runnable")
        val controllerType = constructor.parameterTypes.single().toString()
        val controllerRegister = constructorInstruction.registerD
        val runnable = classDefBy(constructor.definingClass)
        checkShape("Ljava/lang/Runnable;" in runnable.interfaces, "send callback is not a Runnable")
        val run = runnable.methods.single { it.name == "run" && it.parameterTypes.isEmpty() }
        val scrollCall = run.calls().single {
            it.definingClass == controllerType && it.parameterTypes == listOf("Z") && it.returnType == "V"
        }
        checkShape(run.calls().any { it.name == "isResumed" }, "missing thread lifecycle guard")

        val controller = classDefBy(controllerType)
        val scroll = controller.methods.single { it.name == scrollCall.name && it.parameterTypes == listOf("Z") }
        checkShape(scroll.calls().count {
            it.definingClass == "Landroidx/recyclerview/widget/RecyclerView;" && it.parameterTypes == listOf("I")
        } == 2, "scroll controller no longer has immediate/smooth RecyclerView paths")
        val atLatest = controller.methods.single { method ->
            method.returnType == "Z" && method.parameterTypes.isEmpty() &&
                method.calls().any { it.name == "findFirstCompletelyVisibleItemPosition" } &&
                method.calls().any { it.name == "findFirstVisibleItemPosition" }
        }

        // Reuse only the two registers about to be overwritten by int-to-long.
        // No added registers, reflection, obfuscated names or global scroll interception.
        val low = delay.registerA
        val high = low + 1
        checkShape(low == post.registerE && high == post.registerF &&
            high <= 15 && controllerRegister <= 15 &&
            setOf(low, high).intersect(setOf(post.registerC, post.registerD, delay.registerB)).isEmpty(),
            "delay registers cannot safely hold the guard")
        checkShape(completed.getInstruction(postIndex + 1).opcode != Opcode.MOVE_RESULT,
            "postDelayed result is now consumed")
        completed.addInstructionsWithLabels(
            postIndex - 1,
            """
                invoke-virtual {v$controllerRegister}, $atLatest
                move-result v$low
                move-object/from16 v$high, p3
                invoke-static {v$high, v$low}, $HOOK->shouldKeepPosition(Ljava/lang/Object;Z)Z
                move-result v$low
                if-nez v$low, :dudeks_keep_position
            """.trimIndent(),
            ExternalLabel("dudeks_keep_position", completed.getInstruction(postIndex + 1)),
        )
    }

    // Cross-bundle integration must run after Piko has merged its extensions.
    // A clean Instagram without Piko settings fails explicitly, never with a
    // successfully produced APK containing unresolved runtime references.
    finalize {
        val builder = mutableClassDefByOrNull(SCREEN_BUILDER)
            ?: throw PatchException("Keep DM scroll position requires Piko 'Add settings'. Select it in the same Morphe patching run.")
        val screen = builder.fields.single { it.type == "Landroid/preference/PreferenceScreen;" }
        val helper = builder.fields.single { it.type == HELPER }
        val dm = builder.methods.single { it.name == "dmSection" && it.parameterTypes.isEmpty() && it.returnType == "V" }
        checkShape(dm.implementation!!.registerCount >= 3, "Piko DM settings has insufficient local registers")
        checkShape(classDefBy(HELPER).methods.any {
            it.name == "switchPreference" && it.returnType == "Landroid/preference/Preference;" &&
                it.parameterTypes == listOf("Ljava/lang/String;", "Ljava/lang/String;", "Lapp/morphe/extension/crimera/settings/BooleanSetting;")
        }, "Piko preference API changed")
        dm.addInstructions(0, """
            move-object/from16 v0, p0
            iget-object v1, v0, $helper
            iget-object v0, v0, $screen
            invoke-static {v0, v1}, $HOOK->addPreference(Landroid/preference/PreferenceScreen;$HELPER)V
        """.trimIndent())

        val visibility = mutableClassDefBy("$PIKO_SETTINGS/SettingsStatus;").methods.single {
            it.name == "dmSection" && it.parameterTypes.isEmpty() && it.returnType == "Z"
        }
        checkShape(visibility.implementation!!.registerCount >= 1, "Piko DM visibility has no scratch register")
        visibility.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
    }
}
