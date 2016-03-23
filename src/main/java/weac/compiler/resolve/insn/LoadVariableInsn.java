package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class LoadVariableInsn extends ResolvedInsn {
    private final int varIndex;
    private final WeacType varType;

    public LoadVariableInsn(int varIndex, WeacType type) {
        super(LOAD_LOCAL_VARIABLE);
        this.varIndex = varIndex;
        varType = type;
    }

    public int getVarIndex() {
        return varIndex;
    }

    @Override
    public String toString() {
        return super.toString()+" "+varIndex+" ("+varType+")";
    }

    public WeacType getVarType() {
        return varType;
    }
}
