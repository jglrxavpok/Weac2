package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class SubtractInsn extends OperationInsn {
    public SubtractInsn(WeacType type) {
        super(SUBTRACT, type);
    }
}
