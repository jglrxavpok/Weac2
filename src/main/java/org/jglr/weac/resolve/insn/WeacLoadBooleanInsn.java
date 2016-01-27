package org.jglr.weac.resolve.insn;

public class WeacLoadBooleanInsn extends WeacResolvedInsn {

    private final boolean number;

    public WeacLoadBooleanInsn(boolean number) {
        super(LOAD_BOOL_CONSTANT);
        this.number = number;
    }

    public boolean getValue() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
