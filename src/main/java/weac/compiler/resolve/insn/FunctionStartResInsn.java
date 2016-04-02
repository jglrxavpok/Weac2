package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class FunctionStartResInsn extends ResolvedInsn {
    private final String functionName;
    private final int argCount;
    private final WeacType owner;

    public FunctionStartResInsn(String functionName, int argCount, WeacType owner) {
        super(FUNCTION_START);
        this.functionName = functionName;
        this.argCount = argCount;
        this.owner = owner;
    }

    public int getArgCount() {
        return argCount;
    }

    public String getFunctionName() {
        return functionName;
    }

    public WeacType getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return super.toString()+" "+functionName+" in "+owner+" ("+argCount+" args)";
    }
}
