package weac.compiler.precompile.insn;

public class FunctionStartInsn extends PrecompiledInsn {
    private final String funcName;
    private final int nArgs;
    private final boolean shouldLookForInstance;

    public FunctionStartInsn(String funcName, int nArgs, boolean shouldLookForInstance) {
        super(FUNCTION_START);
        this.funcName = funcName;
        this.nArgs = nArgs;
        this.shouldLookForInstance = shouldLookForInstance;
    }

    public String getFunctionName() {
        return funcName;
    }

    public int getArgCount() {
        return nArgs;
    }

    public boolean shouldLookForInstance() {
        return shouldLookForInstance;
    }
}
