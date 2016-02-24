package weac.compiler.resolve.insn;

public class LoadVariableInsn extends ResolvedInsn {
    private final int varIndex;

    public LoadVariableInsn(int varIndex) {
        super(LOAD_LOCAL_VARIABLE);
        this.varIndex = varIndex;
    }

    public int getVarIndex() {
        return varIndex;
    }

    @Override
    public String toString() {
        return super.toString()+" "+varIndex;
    }
}
