package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class MultiplyInsn extends OperationInsn {
    public MultiplyInsn(WeacType type) {
        super(MULTIPLY, type);
    }
}
