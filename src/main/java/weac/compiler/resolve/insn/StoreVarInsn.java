package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class StoreVarInsn extends ResolvedInsn {
    private final int localIndex;
    private final WeacType varType;

    public StoreVarInsn(int localIndex, WeacType varType) {
        super(STORE_LOCAL_VARIABLE);
        this.localIndex = localIndex;
        this.varType = varType;
    }

    public int getLocalIndex() {
        return localIndex;
    }

    @Override
    public String toString() {
        return super.toString()+" "+localIndex+" ("+varType+")";
    }

    public WeacType getVarType() {
        return varType;
    }
}
