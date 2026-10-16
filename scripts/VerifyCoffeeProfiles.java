import app.morphe.patcher.PackageMetadata;
import app.template.patches.coffeebusiness.CoffeeProfiles;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** Checks packaged target selection and both native identities without running a game. */
public class VerifyCoffeeProfiles {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        var profiles = CoffeeProfiles.INSTANCE;
        var seen = new HashSet<String>();
        for (var profile : profiles.getProfiles()) {
            var metadata = new PackageMetadata("com.tapblaze.coffeebusiness", profile.getVersion(), profile.getVersionCode(), null);
            check(profiles.forPackage(metadata) == profile, "Wrong initialization selected");
            check(seen.add(profile.getVersion()+"/"+profile.getVersionCode()), "Duplicate target");
            for (String name : List.of("libunity.so.delta.gz", "libil2cpp.so.delta.gz")) {
                try (var in = new GZIPInputStream(profile.resource(name))) {
                    byte[] header = in.readNBytes(68);
                    check(header.length == 68 && Arrays.equals(Arrays.copyOf(header,4), new byte[]{'P','Z','B','2'}), "Invalid native identity");
                    check(!Arrays.equals(Arrays.copyOfRange(header,4,36), Arrays.copyOfRange(header,36,68)), "Empty native reconstruction");
                }
            }
            for (String name : List.of("strings.tsv.gz", "sdk-receivers.dex", "startup-methods.dex", "firebase-app.dex", "asset-packs.tsv.gz"))
                try (var in = profile.resource(name)) { check(in.read() != -1, "Empty resource"); }
            System.out.println("PASS "+profile.getVersion()+" / "+profile.getVersionCode());
        }
        for (var metadata : List.of(
                new PackageMetadata("com.tapblaze.coffeebusiness", "1.25.1", "1397", null),
                new PackageMetadata("com.tapblaze.coffeebusiness", "99.0.0", "9999", null),
                new PackageMetadata("other.package", "1.25.1", "1454", null))) {
            try { profiles.forPackage(metadata); throw new AssertionError("Mismatched manifest accepted"); }
            catch (Exception expected) { check(expected instanceof app.morphe.patcher.patch.PatchException && expected.getMessage().contains(metadata.getVersionName()), "Missing diagnosis"); }
        }
        System.out.println("PASS mismatched package/version/code rejected");
    }
}
