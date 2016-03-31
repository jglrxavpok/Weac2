package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class LessOrEqualInsn extends OperationInsn {
    public LessOrEqualInsn(WeacType type) {
        super(LESS_OR_EQUAL, type);
    }
}
