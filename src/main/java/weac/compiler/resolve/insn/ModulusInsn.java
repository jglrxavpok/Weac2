package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class ModulusInsn extends OperationInsn {
    public ModulusInsn(WeacType type) {
        super(MODULUS, type);
    }
}
