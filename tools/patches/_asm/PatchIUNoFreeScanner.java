import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * IUCore.loginPlayer gives free veinsencor (ore scanner) on first join. Remove that give;
 * keep the guide-book give.
 */
public final class PatchIUNoFreeScanner {
  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("usage: PatchIUNoFreeScanner <in.class> <out.class>");
      System.exit(2);
    }
    byte[] in = Files.readAllBytes(Path.of(args[0]));
    Files.write(Path.of(args[1]), patch(in));
  }

  static byte[] patch(byte[] classBytes) {
    ClassReader cr = new ClassReader(classBytes);
    ClassNode cn = new ClassNode();
    cr.accept(cn, 0);
    int removed = 0;
    for (MethodNode mn : cn.methods) {
      if (!"loginPlayer".equals(mn.name)) continue;
      AbstractInsnNode insn = mn.instructions.getFirst();
      while (insn != null) {
        AbstractInsnNode next = insn.getNext();
        // getstatic IUItem.veinsencor
        if (insn.getOpcode() == Opcodes.GETSTATIC
            && insn instanceof FieldInsnNode fin
            && "com/denfop/IUItem".equals(fin.owner)
            && "veinsencor".equals(fin.name)) {
          // Walk back to matching aload that starts the give block:
          // aload; getEntity; new; dup; getstatic veinsencor; ...
          AbstractInsnNode start = findGiveBlockStart(fin);
          AbstractInsnNode end = findAfterAddItem(fin);
          if (start != null && end != null) {
            AbstractInsnNode cur = start;
            while (cur != null) {
              AbstractInsnNode n = cur.getNext();
              mn.instructions.remove(cur);
              if (cur == end) break;
              cur = n;
            }
            removed++;
            insn = next;
            continue;
          }
        }
        insn = next;
      }
    }
    if (removed == 0) {
      throw new IllegalStateException("veinsencor give not found in loginPlayer");
    }
    System.out.println("  IUCore.loginPlayer: removed " + removed + " free ore-scanner give(s)");
    ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
    cn.accept(cw);
    return cw.toByteArray();
  }

  /** aload_x ; invokevirtual getEntity ; new ItemStack  immediately before getstatic veinsencor path */
  private static AbstractInsnNode findGiveBlockStart(FieldInsnNode veinsencorGet) {
    AbstractInsnNode p = veinsencorGet.getPrevious();
    // expect: dup, new, getEntity, aload
    // actual order before getstatic: dup, new ItemStack, invoke getEntity, aload
    // From javap:
    // aload_1
    // invokevirtual getEntity
    // new ItemStack
    // dup
    // getstatic veinsencor
    while (p != null && p.getOpcode() == Opcodes.DUP) p = p.getPrevious();
    if (p == null || p.getOpcode() != Opcodes.NEW) return null;
    p = p.getPrevious();
    if (p == null || !(p instanceof MethodInsnNode min) || !"getEntity".equals(min.name)) return null;
    p = p.getPrevious();
    if (p == null) return null;
    int op = p.getOpcode();
    // ALOAD / ALOAD_0..3 (42..45)
    boolean isAload = op == Opcodes.ALOAD || (op >= 42 && op <= 45);
    if (!isAload) return null;
    return p;
  }

  private static AbstractInsnNode findAfterAddItem(FieldInsnNode veinsencorGet) {
    AbstractInsnNode p = veinsencorGet.getNext();
    while (p != null) {
      if (p instanceof MethodInsnNode min
          && "m_36356_".equals(min.name)
          && "net/minecraft/world/entity/player/Player".equals(min.owner)) {
        AbstractInsnNode pop = p.getNext();
        if (pop != null && pop.getOpcode() == Opcodes.POP) return pop;
        return p;
      }
      p = p.getNext();
    }
    return null;
  }

  private PatchIUNoFreeScanner() {}
}
