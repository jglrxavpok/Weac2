package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class SubtractInsn extends ResolvedInsn {
    private final WeacType resultType;

    public SubtractInsn(WeacType type) {
        super(SUBTRACT);
        resultType = type;
    }

    public WeacType getResultType() {
        return resultType;
    }

    @Override
    public String toString() {
        return super.toString()+" "+resultType;
    }
}
