package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public abstract class OperationInsn extends ResolvedInsn {

    private final WeacType resultType;

    public OperationInsn(int opcode, WeacType type) {
        super(opcode);
        this.resultType = type;
    }

    public WeacType getResultType() {
        return resultType;
    }

    @Override
    public String toString() {
        return super.toString()+" "+resultType;
    }
}
