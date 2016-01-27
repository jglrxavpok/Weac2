package org.jglr.weac.resolve.insn;

public class WeacLoadDoubleInsn extends WeacResolvedInsn {

    private final double number;

    public WeacLoadDoubleInsn(double number) {
        super(LOAD_BYTE_CONSTANT);
        this.number = number;
    }

    public double getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
