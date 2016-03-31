package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class LessInsn extends OperationInsn {
    public LessInsn(WeacType type) {
        super(LESS, type);
    }
}
