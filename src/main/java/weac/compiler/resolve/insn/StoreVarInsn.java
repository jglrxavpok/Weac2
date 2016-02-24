package weac.compiler.resolve.insn;

public class StoreVarInsn extends ResolvedInsn {
    private final int localIndex;

    public StoreVarInsn(int localIndex) {
        super(STORE_LOCAL_VARIABLE);
        this.localIndex = localIndex;
    }

    public int getLocalIndex() {
        return localIndex;
    }

    @Override
    public String toString() {
        return super.toString()+" "+localIndex;
    }
}
