package org.jglr.weac.precompile.insn;

public class WeacFunctionCall extends WeacPrecompiledInsn {
    private final String name;
    private final int argCount;
    private final boolean lookForInstance;

    public WeacFunctionCall(String name, int argCount, boolean lookForInstance) {
        super(FUNCTION_CALL);
        this.name = name;
        this.argCount = argCount;
        this.lookForInstance = lookForInstance;
    }

    public boolean shouldLookForInstance() {
        return lookForInstance;
    }

    public int getArgCount() {
        return argCount;
    }

    public String getName() {
        return name;
    }
}
