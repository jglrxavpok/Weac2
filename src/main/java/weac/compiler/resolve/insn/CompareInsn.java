package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class CompareInsn extends OperationInsn {
    public CompareInsn(WeacType resultType) {
        super(COMPARE_ZERO, resultType);
    }
}
