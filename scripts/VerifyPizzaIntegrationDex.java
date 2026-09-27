import java.util.*;
import java.util.zip.ZipFile;
import java.security.MessageDigest;
import com.android.apksig.ApkVerifier;
import com.android.tools.smali.dexlib2.*;
import com.android.tools.smali.dexlib2.iface.*;
import com.android.tools.smali.dexlib2.iface.instruction.*;
import com.android.tools.smali.dexlib2.iface.reference.*;
import com.reandroid.arsc.chunk.xml.*;

/** Run with VerifyPizzaRewardDex and Morphe CLI on the classpath.
 * Arguments: patched.apk original.apk [microg|stock]. No Google credentials.
 */
public class VerifyPizzaIntegrationDex extends VerifyPizzaRewardDex {
    static final String LICENSE = "Lcom/pairip/licensecheck/LicenseClient;";
    static final String SCREEN = "Lcom/pairip/licensecheck/LicenseActivity;";
    static final String GMS = "app.revanced.android.gms";
    static final String CLIENT = "Lcom/google/android/gms/common/internal/f;";
    static final Map<String,String> CLIENTS = Map.of(
        "Lcom/google/android/gms/internal/games_v2/zzp;", "games.internal.connect.service.START",
        "Lld/d;", "games.service.START");

    static void noEffect(Method m) {
        validateRegistersAndBranches(m);
        check(code(m).size() == 1 && code(m).get(0).getOpcode() == Opcode.RETURN_VOID &&
            m.getImplementation().getTryBlocks().isEmpty(), "Licensing side effect remains: " + key(m));
    }

    static String constant(Method m) {
        validateRegistersAndBranches(m);
        var c = code(m);
        check(c.size() == 2 && c.get(1).getOpcode() == Opcode.RETURN_OBJECT &&
            (c.get(0).getOpcode() == Opcode.CONST_STRING || c.get(0).getOpcode() == Opcode.CONST_STRING_JUMBO) &&
            ((OneRegisterInstruction)c.get(0)).getRegisterA() == ((OneRegisterInstruction)c.get(1)).getRegisterA(),
            "Not a constant string return: " + key(m));
        return ((StringReference)((ReferenceInstruction)c.get(0)).getReference()).getString();
    }

    static void transportOnlyFalse(Method m) {
        validateRegistersAndBranches(m);
        var c = code(m);
        check(c.size() == 2 && c.get(0).getOpcode() == Opcode.CONST_4 &&
            ((NarrowLiteralInstruction)c.get(0)).getNarrowLiteral() == 0 && c.get(1).getOpcode() == Opcode.RETURN &&
            ((OneRegisterInstruction)c.get(0)).getRegisterA() == ((OneRegisterInstruction)c.get(1)).getRegisterA(),
            "Wrong transport override: " + key(m));
    }

    static String attribute(ResXmlElement e, String name) {
        var a = e.searchAttributeByName(name);
        return a == null ? null : a.getValueAsString();
    }

    static Map<String,String> metadata(AndroidManifestBlock manifest) {
        Map<String,String> result = new HashMap<>();
        var it = manifest.getApplicationElement().getElements("meta-data");
        while (it.hasNext()) {
            var e = (ResXmlElement)it.next();
            result.put(attribute(e,"name"), attribute(e,"value"));
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        check(args.length == 3 && Set.of("microg","stock").contains(args[2]),
            "Usage: VerifyPizzaIntegrationDex patched.apk original.apk microg|stock");
        boolean microg = args[2].equals("microg");
        var patched = classes(args[0]);
        var original = classes(args[1]);
        List<Method> blocked = List.of(
            method(patched, LICENSE, "checkLicense", "V", "Landroid/content/Context;"),
            method(patched, LICENSE, "stopTrial", "V", "Landroid/content/Context;"),
            method(patched, LICENSE, "handleTrialEnd", "V"),
            method(patched, LICENSE, "initializeLicenseCheck", "V"),
            method(patched, LICENSE, "bindToLicensingService", "V", "Z"),
            method(patched, LICENSE, "processResponse", "V", "I", "Landroid/os/Bundle;"),
            method(patched, LICENSE, "scheduleRepeatedLicenseCheck", "V", "Lcom/pairip/licensecheck/RepeatedCheckMetadata;"),
            method(patched, LICENSE, "startPaywallActivity", "V", "Landroid/app/PendingIntent;"),
            method(patched, LICENSE, "startErrorDialogActivity", "V"),
            method(patched, LICENSE, "scheduleAppShutdown", "V"));
        blocked.forEach(VerifyPizzaIntegrationDex::noEffect);
        Method screen = method(patched, SCREEN, "onStart", "V");
        validateRegistersAndBranches(screen);
        var sc = code(screen);
        check(sc.size() == 3 && sc.get(0).getOpcode() == Opcode.INVOKE_SUPER &&
            key(call(sc.get(0))).equals("Landroid/app/Activity;->onStart()V") &&
            sc.get(1).getOpcode() == Opcode.INVOKE_VIRTUAL &&
            key(call(sc.get(1))).equals("Landroid/app/Activity;->finish()V") &&
            registers(sc.get(0)).equals(List.of(screen.getImplementation().getRegisterCount()-1)) &&
            registers(sc.get(1)).equals(registers(sc.get(0))) && sc.get(2).getOpcode() == Opcode.RETURN_VOID &&
            screen.getImplementation().getTryBlocks().isEmpty(), "Restored screen can open Play or close game tasks");

        Set<String> allowed = new HashSet<>();
        if (microg) {
            for (var entry : CLIENTS.entrySet()) {
                String owner = entry.getKey();
                Method action = method(patched, owner, "getStartServiceAction", STRING);
                allowed.add(key(action));
                check(constant(action).equals(GMS + "." + entry.getValue()), "Wrong Games service action");
                check(constant(method(patched, owner, "getStartServicePackage", STRING)).equals(GMS), "Wrong Games host");
                for (String name : List.of("getUseDynamicLookup", "requiresGooglePlayServices")) {
                    transportOnlyFalse(method(patched, owner, name, "Z"));
                }
                for (String name : List.of("getStartServicePackage", "getUseDynamicLookup", "requiresGooglePlayServices")) {
                    Method m = method(patched, owner, name, name.equals("getStartServicePackage") ? STRING : "Z");
                    check(AccessFlags.PUBLIC.isSet(m.getAccessFlags()) && !AccessFlags.STATIC.isSet(m.getAccessFlags()),
                        "Override has wrong access flags");
                }
            }
            Method installed = method(patched, ACTIVITY, "isGooglePlayGamesInstalled", "Z");
            allowed.add(key(installed));
            check(code(installed).stream().anyMatch(i -> i instanceof ReferenceInstruction r &&
                r.getReference() instanceof StringReference s && s.getString().equals(GMS)), "Presence check uses stock Games");
            check(code(installed).stream().anyMatch(i -> key(call(i)).equals(
                "Landroid/content/pm/PackageManager;->getApplicationInfo(Ljava/lang/String;I)Landroid/content/pm/ApplicationInfo;")),
                "Presence check was replaced with fake success");
        }

        // Whole-class comparison: authentication callbacks, server-auth requests,
        // player IDs, errors, Binder descriptors and SDK/VM return values survive.
        int preserved = 0, protectedMethods = 0;
        for (ClassDef cls : original.values()) {
            boolean auth = cls.getType().equals(ACTIVITY) || cls.getType().equals(CLIENT) ||
                cls.getType().startsWith("Lcom/google/android/gms/internal/games_v2/") ||
                cls.getType().startsWith("Lld/") || cls.getType().equals("Lcom/pairip/VMRunner;");
            for (Method before : cls.getMethods()) {
                boolean vm = before.getImplementation() != null && code(before).stream().anyMatch(i ->
                    call(i) != null && call(i).getDefiningClass().equals("Lcom/pairip/VMRunner;") && call(i).getName().equals("invoke"));
                if (vm && !cls.getType().startsWith("Lcom/pairip/")) protectedMethods++;
                if ((!auth && !(vm && !cls.getType().startsWith("Lcom/pairip/"))) || allowed.contains(key(before))) continue;
                Method after = method(patched, cls.getType(), before.getName(), before.getReturnType(),
                    before.getParameterTypes().stream().map(Object::toString).toArray(String[]::new));
                check(canonical(before).equals(canonical(after)), "Unrelated/auth/protected method changed: " + key(before));
                preserved++;
            }
        }
        check(protectedMethods == 20, "Protected SDK method count changed");

        try (ZipFile oldZip = new ZipFile(args[1]); ZipFile newZip = new ZipFile(args[0])) {
            var before = AndroidManifestBlock.load(oldZip.getInputStream(oldZip.getEntry("AndroidManifest.xml")));
            var after = AndroidManifestBlock.load(newZip.getInputStream(newZip.getEntry("AndroidManifest.xml")));
            check(before.getPackageName().equals(after.getPackageName()) && before.getVersionCode().equals(after.getVersionCode()),
                "Game identity changed");
            var oldMeta = metadata(before); var newMeta = metadata(after);
            for (var entry : oldMeta.entrySet()) check(Objects.equals(newMeta.get(entry.getKey()), entry.getValue()),
                "Original metadata changed: " + entry.getKey());
            if (microg) {
                var sig = new ApkVerifier.Builder(new java.io.File(args[1])).build().verify();
                check(sig.isVerified() && sig.getSignerCertificates().size() == 1, "Original APK signature not verified");
                String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1")
                    .digest(sig.getSignerCertificates().get(0).getEncoded()));
                check(Objects.equals(newMeta.get(GMS+".SPOOFED_PACKAGE_SIGNATURE"), digest), "Wrong original signer metadata");
                check(Objects.equals(newMeta.get(GMS+".SPOOFED_PACKAGE_NAME"), before.getPackageName()), "Wrong original package metadata");
                var queries = after.getManifestElement().getElement("queries").getElements("package");
                boolean visible = false;
                while (queries.hasNext()) if (GMS.equals(attribute((ResXmlElement)queries.next(),"name"))) visible = true;
                check(visible, "MicroG not visible on Android 11+");
            } else check(!newMeta.containsKey(GMS+".SPOOFED_PACKAGE_NAME"), "Stock variant unexpectedly enables MicroG");
            for (var entries = oldZip.entries(); entries.hasMoreElements();) {
                var e = entries.nextElement();
                if (!(e.getName().startsWith("lib/") || e.getName().startsWith("assets/"))) continue;
                check(newZip.getEntry(e.getName()) != null && Arrays.equals(oldZip.getInputStream(e).readAllBytes(),
                    newZip.getInputStream(newZip.getEntry(e.getName())).readAllBytes()), "Native code/assets changed: " + e.getName());
            }
        }
        System.out.println("PASS: 10 licensing entry/retry/remediation/shutdown bodies have no side effects; restored screen only finishes itself");
        System.out.println("PASS: " + preserved + " unchanged authentication/SDK methods, including 20 protected VM callers");
        System.out.println("PASS: " + args[2] + " transport, original app identity, signer metadata, package visibility and native/assets integrity");
        System.out.println("LIMIT: these checks do not log in to Google or write/read a cloud save on a phone");
    }
}
