package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class NegateInstruction extends ResolvedInsn {
    private final WeacType type;

    public NegateInstruction(WeacType type) {
        super(NEGATE);
        this.type = type;
    }

    public WeacType getValueType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString()+" "+type;
    }
}
