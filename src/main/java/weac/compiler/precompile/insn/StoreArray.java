package weac.compiler.precompile.insn;

public class StoreArray extends PrecompiledInsn {
    private final int index;

    public StoreArray(int index) {
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
        if(obj instanceof PrecompiledInsn) {
            StoreArray casted = ((StoreArray) obj);
            return casted.getIndex() == index;
        }
        return false;
    }
}
