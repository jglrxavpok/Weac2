package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class DivideInsn extends OperationInsn {
    public DivideInsn(WeacType type) {
        super(DIVIDE, type);
    }
}
