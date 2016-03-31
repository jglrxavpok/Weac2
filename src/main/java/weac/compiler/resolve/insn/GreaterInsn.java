package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class GreaterInsn extends OperationInsn {
    public GreaterInsn(WeacType type) {
        super(GREATER, type);
    }
}
