package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class GreaterOrEqualInsn extends OperationInsn {
    public GreaterOrEqualInsn(WeacType type) {
        super(GREATER_OR_EQUAL, type);
    }
}
