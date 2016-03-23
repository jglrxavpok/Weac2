package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class MultiplyInsn extends ResolvedInsn {
    private final WeacType resultType;

    public MultiplyInsn(WeacType type) {
        super(MULTIPLY);
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
