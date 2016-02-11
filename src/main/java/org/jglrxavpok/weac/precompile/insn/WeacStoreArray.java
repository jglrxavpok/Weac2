package org.jglrxavpok.weac.precompile.insn;

public class WeacStoreArray extends WeacPrecompiledInsn {
    private final int index;

    public WeacStoreArray(int index) {
        super(STORE_ARRAY);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return super.toString() + " "+index;
    }
}