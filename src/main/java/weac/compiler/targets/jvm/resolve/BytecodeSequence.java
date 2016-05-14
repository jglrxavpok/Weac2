package weac.compiler.targets.jvm.resolve;

import org.objectweb.asm.MethodVisitor;

@FunctionalInterface
public interface BytecodeSequence {

    void write(MethodVisitor visitor, int varIndexOffset);
}
