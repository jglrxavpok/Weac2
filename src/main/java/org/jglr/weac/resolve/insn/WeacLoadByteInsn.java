package org.jglr.weac.resolve.insn;

public class WeacLoadByteInsn extends WeacResolvedInsn {

    private final byte number;

    public WeacLoadByteInsn(byte number) {
        super(LOAD_BYTE_CONSTANT);
        this.number = number;
    }

    public byte getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
