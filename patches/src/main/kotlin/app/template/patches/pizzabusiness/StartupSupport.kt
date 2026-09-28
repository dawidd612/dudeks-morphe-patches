package app.template.patches.pizzabusiness

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.replaceBody
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

/** Narrow startup companion. Preserve protected SDK methods and their return types. */
internal val pizzaStartupSupport = bytecodePatch {
    dependsOn(pizzaNativeBootstrap)
    execute {
        val context = "Landroid/content/Context;"
        val signature = "Lcom/pairip/SignatureCheck;"
        val launcher = "Lcom/pairip/StartupLauncher;"
        val license = "Lcom/pairip/licensecheck/LicenseClient;"
        val vm = "Lcom/pairip/VMRunner;"
        val attach = pizzaMethod("Lcom/pairip/application/Application;", "attachBaseContext", listOf(context), "V", static = false)
        val integrity = pizzaMethod(signature, "verifyIntegrity", listOf(context), "V")
        val startup = pizzaMethod(launcher, "launch", emptyList(), "V")
        val checkLicense = pizzaMethod(license, "checkLicense", listOf(context), "V")
        val program = classDefBy(launcher).fields.singleOrNull { it.name == "startupProgramName" && it.type == STRING }
        requirePizza(attach.calls(signature, "verifyIntegrity") && attach.calls(license, "checkLicense") &&
            attach.calls(vm, "setContext"), "application startup changed")
        requirePizza(integrity.hasString("Apk signature is invalid.") && startup.calls(vm, "invoke") &&
            (program?.initialValue as? StringEncodedValue)?.value == "87jzgtv0i2Sz3iNG" && checkLicense.implementation != null &&
            checkLicense.instructions.size > 1, "re-signed startup shape changed or already patched")

        // checkLicense is not the only entry: TrialClient/JNI and queued retries
        // can enter the client directly. Guard the whole local licensing path
        // before changing any body. Do not invent a signed license response.
        val stopTrial = pizzaMethod(license, "stopTrial", listOf(context), "V")
        val initialize = pizzaMethod(license, "initializeLicenseCheck", emptyList(), "V", static = false)
        val trialEnd = pizzaMethod(license, "handleTrialEnd", emptyList(), "V", static = false)
        val bind = pizzaMethod(license, "bindToLicensingService", listOf("Z"), "V", static = false)
        val response = pizzaMethod(license, "processResponse", listOf("I", "Landroid/os/Bundle;"), "V", static = false)
        val repeat = pizzaMethod(license, "scheduleRepeatedLicenseCheck",
            listOf("Lcom/pairip/licensecheck/RepeatedCheckMetadata;"), "V", static = false)
        val paywall = pizzaMethod(license, "startPaywallActivity", listOf("Landroid/app/PendingIntent;"), "V", static = false)
        val error = pizzaMethod(license, "startErrorDialogActivity", emptyList(), "V", static = false)
        val shutdown = pizzaMethod(license, "scheduleAppShutdown", emptyList(), "V", static = false)
        val screen = pizzaMethod("Lcom/pairip/licensecheck/LicenseActivity;", "onStart", emptyList(), "V", static = false)
        requirePizza(stopTrial.hasString("Cannot trigger trial end with null context.") &&
            trialEnd.calls(license, "initiateFreshLicensingServiceConnection") &&
            initialize.calls(license, "initiateFreshLicensingServiceConnection") &&
            bind.calls(context, "bindService") && response.calls(license, "startPaywallActivity") &&
            repeat.hasString("Repeated license check is scheduled in %d ms...") &&
            paywall.calls(context, "startActivity") && error.calls(context, "startActivity") &&
            shutdown.calls("Lcom/pairip/licensecheck/LicenseClient\$DelayedTaskExecutor;", "schedule") &&
            screen.calls("Landroid/app/Activity;", "onStart") &&
            screen.calls("Lcom/pairip/licensecheck/LicenseActivity;", "showPaywallAndCloseApp"),
            "licensing entry, retry or remediation flow changed")

        // Preserve initialized constants, native code/imports and recovered SDK
        // behavior before replacing the original re-signing startup path.
        restorePizzaBootstrap()

        // Tapjoy's connection worker outlives Activity recreation. Android
        // automatically removes Activity-owned receivers on destruction, then
        // the worker crashes when it performs its normal unregister. Give only
        // this receiver the application lifetime; retain SDK UI contexts,
        // connection retries, callbacks and unregister/finally behavior.
        val worker = pizzaMethod("Lck/f0;", "run", emptyList(), "V", static = false)
        val stores = worker.instructions.withIndex().filter { (_, instruction) ->
            val field = (instruction as? ReferenceInstruction)?.reference as? FieldReference
            instruction.opcode == Opcode.IPUT_OBJECT && field?.definingClass == "Lck/f0;" && field.name == "e"
        }
        requirePizza(stores.size == 1 && worker.hasString("android.net.conn.CONNECTIVITY_CHANGE") &&
            worker.calls(context, "registerReceiver") && worker.calls(context, "unregisterReceiver"),
            "Tapjoy connection receiver lifetime changed")
        val store = stores.single()
        val receiverContext = (store.value as TwoRegisterInstruction).registerA
        worker.addInstructions(store.index, """
            invoke-virtual {v$receiverContext}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;
            move-result-object v$receiverContext
        """.trimIndent())
        // VMRunner and all remaining protected callers retain their return types.
        integrity.replaceBody("return-void")
        startup.replaceBody("return-void")
        checkLicense.replaceBody("return-void")
        listOf(stopTrial, initialize, trialEnd, bind, response, repeat, paywall, error, shutdown)
            .forEach { it.replaceBody("return-void") }
        // A saved Android task can restore this activity independently of a new
        // license request. Finish only this activity, never the game's tasks.
        screen.replaceBody("""
            invoke-super {p0}, Landroid/app/Activity;->onStart()V
            invoke-virtual {p0}, Landroid/app/Activity;->finish()V
            return-void
        """.trimIndent())
    }
}
