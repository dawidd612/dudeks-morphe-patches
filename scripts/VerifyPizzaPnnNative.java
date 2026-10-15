import app.template.patches.pizzabusiness.PizzaPnnNative;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Arrays;

/** Run on the real restored library, not a synthetic hash-bypass fixture. */
public final class VerifyPizzaPnnNative {
    public static void main(String[] args) throws Exception {
        byte[] original = Files.readAllBytes(Path.of(args[0]));
        byte[] payload = Base64.getDecoder().decode(Files.readString(Path.of(args[1])).trim());
        byte[] output = PizzaPnnNative.INSTANCE.apply(original, payload);
        byte[] corrupt = original.clone();
        corrupt[0x1369aa0] ^= 1;
        for (byte[] invalid : new byte[][] {corrupt, output, Arrays.copyOf(original, 1024)}) {
            try {
                PizzaPnnNative.INSTANCE.apply(invalid, payload);
                throw new AssertionError("Accepted corrupt/unknown/already-patched input");
            } catch (IllegalArgumentException expected) {}
        }
        if (!Arrays.equals(original, Files.readAllBytes(Path.of(args[0])))) {
            throw new AssertionError("Mutated original input");
        }
        Files.write(Path.of(args[2]), output);
        System.out.println("PASS: production Kotlin transform; corrupt/unknown/already-patched inputs rejected");
    }
}
