package org.jglrxavpok.weac.precompile.insn;

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

    @Override
    public String toString() {
        return super.toString() + " "+name+", "+argCount+" arguments, shouldLookForInstance="+lookForInstance;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj))
            return false;
        if(obj instanceof WeacPrecompiledInsn) {
            WeacFunctionCall casted = ((WeacFunctionCall) obj);
            return casted.getName().equals(name) && casted.getArgCount() == argCount && casted.shouldLookForInstance() == lookForInstance;
        }
        return false;
    }
}
