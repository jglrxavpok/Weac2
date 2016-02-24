package weac.compiler.resolve.insn;

public class WeacLoadVariableInsn extends WeacResolvedInsn {
    private final int varIndex;

    public WeacLoadVariableInsn(int varIndex) {
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
