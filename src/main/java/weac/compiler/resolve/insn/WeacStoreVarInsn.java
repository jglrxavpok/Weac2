package weac.compiler.resolve.insn;

public class WeacStoreVarInsn extends WeacResolvedInsn {
    private final int localIndex;

    public WeacStoreVarInsn(int localIndex) {
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
