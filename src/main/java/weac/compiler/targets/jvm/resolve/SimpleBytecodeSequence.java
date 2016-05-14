package weac.compiler.targets.jvm.resolve;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SimpleBytecodeSequence implements BytecodeSequence {

    private int opcode;

    public SimpleBytecodeSequence(int opcode) {
        this.opcode = opcode;
    }

    @Override
    public void write(MethodVisitor visitor, int varIndexOffset) {
        visitor.visitInsn(opcode);
    }
}
