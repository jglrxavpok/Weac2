package org.jglrxavpok.weac.resolve.insn;

public class WeacLoadIntInsn extends WeacResolvedInsn {

    private final int number;

    public WeacLoadIntInsn(int number) {
        super(LOAD_INTEGER_CONSTANT);
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
