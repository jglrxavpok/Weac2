package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class AddInsn extends OperationInsn {
    public AddInsn(WeacType type) {
        super(ADD, type);
    }
}
