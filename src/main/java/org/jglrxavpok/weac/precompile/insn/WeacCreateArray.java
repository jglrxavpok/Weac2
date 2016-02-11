package org.jglrxavpok.weac.precompile.insn;

public class WeacCreateArray extends WeacPrecompiledInsn {
    private final int length;
    private final String type;

    public WeacCreateArray(int length, String type) {
        super(CREATE_ARRAY);
        this.length = length;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return super.toString() + ' ' + length+" ("+type+")";
    }
}
