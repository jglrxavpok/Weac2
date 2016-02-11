package org.jglrxavpok.weac.resolve.insn;

public class WeacLoadShortInsn extends WeacResolvedInsn {

    private final short number;

    public WeacLoadShortInsn(short number) {
        super(LOAD_SHORT_CONSTANT);
        this.number = number;
    }

    public short getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
