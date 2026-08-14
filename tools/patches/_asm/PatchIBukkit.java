import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Replace IllegalArgumentException throws for non-minecraft ids with return null.
 * Recomputes StackMapTable via ASM COMPUTE_FRAMES.
 */
public final class PatchIBukkit {
  private static final Set<String> MSGS =
      Set.of(
          "Bukkit only supports Minecraft blocks",
          "Bukkit only supports Minecraft items",
          "Bukkit only supports vanilla entities",
          "Bukkit only supports vanilla biomes");

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("usage: PatchIBukkit <in.class> <out.class>");
      System.exit(2);
    }
    byte[] in = Files.readAllBytes(Path.of(args[0]));
    byte[] out = patch(in);
    Files.write(Path.of(args[1]), out);
  }

  static byte[] patch(byte[] classBytes) {
    ClassReader cr = new ClassReader(classBytes);
    ClassNode cn = new ClassNode();
    cr.accept(cn, 0);
    int hits = 0;
    for (MethodNode mn : cn.methods) {
      hits += patchMethod(mn);
    }
    if (hits == 0) {
      throw new IllegalStateException("no throw sites patched");
    }
    System.out.println("  IBukkitAdapter: patched " + hits + " throw sites -> return null (ASM)");
    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
    cn.accept(cw);
    return cw.toByteArray();
  }

  private static int patchMethod(MethodNode mn) {
    int hits = 0;
    AbstractInsnNode insn = mn.instructions.getFirst();
    while (insn != null) {
      AbstractInsnNode next = insn.getNext();
      if (insn.getOpcode() == Opcodes.NEW
          && insn instanceof TypeInsnNode tin
          && "java/lang/IllegalArgumentException".equals(tin.desc)) {
        AbstractInsnNode dup = next;
        if (dup == null || dup.getOpcode() != Opcodes.DUP) {
          insn = next;
          continue;
        }
        AbstractInsnNode ldc = dup.getNext();
        if (!(ldc instanceof LdcInsnNode lin) || !(lin.cst instanceof String s) || !MSGS.contains(s)) {
          insn = next;
          continue;
        }
        AbstractInsnNode init = ldc.getNext();
        if (!(init instanceof MethodInsnNode min)
            || init.getOpcode() != Opcodes.INVOKESPECIAL
            || !"java/lang/IllegalArgumentException".equals(min.owner)
            || !"<init>".equals(min.name)) {
          insn = next;
          continue;
        }
        AbstractInsnNode athrow = init.getNext();
        if (athrow == null || athrow.getOpcode() != Opcodes.ATHROW) {
          insn = next;
          continue;
        }
        AbstractInsnNode after = athrow.getNext();
        InsnList repl = new InsnList();
        repl.add(new InsnNode(Opcodes.ACONST_NULL));
        repl.add(new InsnNode(Opcodes.ARETURN));
        mn.instructions.insertBefore(insn, repl);
        mn.instructions.remove(insn);
        mn.instructions.remove(dup);
        mn.instructions.remove(ldc);
        mn.instructions.remove(init);
        mn.instructions.remove(athrow);
        hits++;
        insn = after;
        continue;
      }
      insn = next;
    }
    return hits;
  }

  private PatchIBukkit() {}
}
