package weac.compiler.precompile.insn;

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

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj))
            return false;
        if(obj instanceof WeacPrecompiledInsn) {
            WeacStoreArray casted = ((WeacStoreArray) obj);
            return casted.getIndex() == index;
        }
        return false;
    }
}
