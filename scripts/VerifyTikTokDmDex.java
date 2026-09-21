import java.nio.ByteBuffer;
import java.util.*;
import java.util.zip.ZipFile;
import com.android.tools.smali.dexlib2.*;
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile;
import com.android.tools.smali.dexlib2.iface.*;
import com.android.tools.smali.dexlib2.iface.instruction.*;
import com.android.tools.smali.dexlib2.iface.reference.*;

/** Executes the actual injected DEX guard, stopping at the original native code.
 * Usage: javac -cp morphe.jar -d /tmp/dm-test scripts/VerifyTikTokDmDex.java
 * java -cp morphe.jar:/tmp/dm-test VerifyTikTokDmDex patched.apk
 * This tests register/branch wiring, not Android rendering or native internals.
 */
public class VerifyTikTokDmDex {
    record Event(String type, boolean message) {}
    static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        String send = null, add = null;
        Method decision = null;
        try (ZipFile zip = new ZipFile(args[0])) {
            for (var entries = zip.entries(); entries.hasMoreElements();) {
                var entry = entries.nextElement();
                if (!entry.getName().endsWith(".dex")) continue;
                var dex = new DexBackedDexFile(Opcodes.getDefault(),
                    ByteBuffer.wrap(zip.getInputStream(entry).readAllBytes()));
                for (ClassDef c : dex.getClasses()) for (Method m : c.getMethods()) {
                    if (m.getImplementation() == null) continue;
                    boolean fake = false, status = false;
                    for (Instruction i : m.getImplementation().getInstructions()) {
                        if (!(i instanceof ReferenceInstruction r)) continue;
                        if (r.getReference() instanceof StringReference s && m.getName().equals("getName")) {
                            if (s.getString().equals("OnSendMessageEvent")) send = c.getType();
                            if (s.getString().equals("OnAddMessageEvent")) add = c.getType();
                        }
                        if (r.getReference() instanceof MethodReference mr) {
                            fake |= mr.getDefiningClass().endsWith("/FakeMessageKt;") && mr.getName().equals("isFakeMessage");
                            status |= mr.getName().equals("getMsgStatus");
                        }
                    }
                    if (m.getReturnType().equals("Z") && m.getParameterTypes().size() == 4
                        && m.getParameterTypes().get(1).toString().equals("Ljava/util/List;")
                        && m.getParameterTypes().get(3).toString().equals("Z") && fake && status
                        && hasLatestCheck(c)) {
                        require(decision == null, "Ambiguous scroll decision: " + decision + " and " + m);
                        decision = m;
                    }
                }
            }
        }
        require(send != null && add != null && decision != null, "Missing fingerprints");
        List<Instruction> code = new ArrayList<>();
        decision.getImplementation().getInstructions().forEach(code::add);
        Map<Integer,Integer> offsets = new HashMap<>();
        int units = 0, original = -1;
        for (int i = 0; i < code.size(); i++) {
            offsets.put(units, i);
            units += code.get(i).getCodeUnits();
            if (original < 0 && code.get(i).getOpcode() == Opcode.RETURN) original = i + 1;
        }
        require(original > 10, "Guard is missing");
        int cases = 0;
        for (String type : Arrays.asList(send, add, "incoming-list", "navigation", null))
            for (boolean message : new boolean[]{false,true})
                for (boolean reply : new boolean[]{false,true})
                    for (boolean self : new boolean[]{false,true})
                        for (boolean latest : new boolean[]{false,true}) {
                            boolean suppressed = run(code, offsets, original,
                                decision.getImplementation().getRegisterCount(),
                                type == null ? null : new Event(type, message), reply, self, latest);
                            boolean expected = (Objects.equals(type, send) || Objects.equals(type, add))
                                && message && reply && self && !latest;
                            require(suppressed == expected, "Wrong result: " + type + ", message=" + message
                                + ", reply=" + reply + ", self=" + self + ", latest=" + latest);
                            cases++;
                        }
        System.out.println("PASS: " + cases + " actual DEX guard cases (send/add/other/null, reply, sender, latest)");
    }
    static boolean hasLatestCheck(ClassDef c) {
        for (Method m : c.getMethods()) {
            if (!m.getParameterTypes().isEmpty() || !m.getReturnType().equals("Z") || m.getImplementation() == null) continue;
            boolean position = false, loadMore = false;
            for (Instruction i : m.getImplementation().getInstructions()) {
                if (i instanceof ReferenceInstruction r && r.getReference() instanceof MethodReference call) {
                    position |= call.getName().equals("findFirstVisibleItemPosition");
                    loadMore |= call.getDefiningClass().equals("Lcom/ss/android/ugc/aweme/im/messagelist/api/ability/LoadMoreAbility;");
                }
            }
            if (position && loadMore) return true;
        }
        return false;
    }
    static boolean zero(Object value) { return value == null || value instanceof Number n && n.longValue() == 0; }
    static boolean run(List<Instruction> code, Map<Integer,Integer> offsets, int original,
                       int count, Event event, boolean reply, boolean self, boolean latest) {
        Object[] regs = new Object[count];
        regs[count - 5] = "controller";
        regs[count - 4] = event;
        Object result = null;
        int pc = 0, address = 0;
        for (int step = 0; step < 60; step++) {
            if (pc == original) return false;
            Instruction i = code.get(pc);
            int a = i instanceof OneRegisterInstruction r ? r.getRegisterA() : -1;
            int b = i instanceof TwoRegisterInstruction r ? r.getRegisterB() : -1;
            boolean branch = false;
            switch (i.getOpcode()) {
                case INSTANCE_OF -> regs[a] = regs[b] instanceof Event e &&
                    e.type.equals(((TypeReference)((ReferenceInstruction)i).getReference()).getType()) ? 1 : 0;
                case MOVE_OBJECT -> regs[a] = regs[b];
                case CHECK_CAST -> require(regs[a] instanceof Event, "Invalid event cast");
                case IGET_OBJECT -> {
                    Event e = (Event)regs[b];
                    FieldReference field = (FieldReference)((ReferenceInstruction)i).getReference();
                    require(e.type.equals(field.getDefiningClass()), "Wrong event message field");
                    regs[a] = e.message ? "message" : null;
                }
                case IF_EQZ -> branch = zero(regs[a]);
                case IF_NEZ -> branch = !zero(regs[a]);
                case GOTO -> branch = true;
                case INVOKE_VIRTUAL, INVOKE_STATIC -> {
                    MethodReference m = (MethodReference)((ReferenceInstruction)i).getReference();
                    int r = ((FiveRegisterInstruction)i).getRegisterC();
                    if (m.getName().equals("getReferenceInfo")) {
                        require("message".equals(regs[r]), "Reference getter receiver");
                        result = reply ? "reference" : null;
                    } else if (m.getName().equals("getSender")) {
                        require("message".equals(regs[r]), "Sender getter receiver");
                        result = 42L;
                    } else if (m.getParameterTypes().equals(List.of("J"))) {
                        require(Long.valueOf(42).equals(regs[r]), "Account check argument");
                        result = self ? 1 : 0;
                    } else {
                        require("controller".equals(regs[r]) && m.getParameterTypes().isEmpty()
                            && m.getReturnType().equals("Z"), "Latest-message check receiver");
                        result = latest ? 1 : 0;
                    }
                }
                case MOVE_RESULT, MOVE_RESULT_OBJECT, MOVE_RESULT_WIDE -> regs[a] = result;
                case CONST_4 -> regs[a] = ((NarrowLiteralInstruction)i).getNarrowLiteral();
                case RETURN -> { require(zero(regs[a]), "Guard forced a scroll"); return true; }
                default -> throw new AssertionError("Unexpected guard opcode: " + i.getOpcode());
            }
            address = branch ? address + ((OffsetInstruction)i).getCodeOffset() : address + i.getCodeUnits();
            require(offsets.containsKey(address), "Invalid branch target");
            pc = offsets.get(address);
        }
        throw new AssertionError("Guard did not terminate");
    }
}
