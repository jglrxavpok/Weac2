package org.jglr.weac.resolve.insn;

public class WeacLoadLongInsn extends WeacResolvedInsn {

    private final long number;

    public WeacLoadLongInsn(long number) {
        super(LOAD_LONG_CONSTANT);
        this.number = number;
    }

    public long getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
