package app.template.patches.pizzabusiness

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.replaceBody

/** Narrow startup companion. Preserve protected SDK methods and their return types. */
internal val pizzaStartupSupport = bytecodePatch {
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
        val launcherInit = pizzaMethod(launcher, "<clinit>", emptyList(), "V")
        requirePizza(attach.calls(signature, "verifyIntegrity") && attach.calls(license, "checkLicense") &&
            attach.calls(vm, "setContext"), "application startup changed")
        requirePizza(integrity.hasString("Apk signature is invalid.") && startup.calls(vm, "invoke") &&
            launcherInit.hasString("87jzgtv0i2Sz3iNG") && checkLicense.implementation != null &&
            checkLicense.instructions.size > 1, "re-signed startup shape changed or already patched")

        // Do not replace VMRunner or its callers: some return values are unboxed.
        // Native VM state after skipping startup still needs an on-device test.
        integrity.replaceBody("return-void")
        startup.replaceBody("return-void")
        checkLicense.replaceBody("return-void")
    }
}
