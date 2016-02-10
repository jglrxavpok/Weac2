package org.jglr.weac.resolve.insn;

import org.jglr.weac.utils.WeacType;

public class WeacFunctionCallInsn extends WeacResolvedInsn {
    private final String name;
    private final WeacType owner;
    private final int nArgs;
    private final boolean instanceInStack;
    private final WeacType[] argTypes;
    private final WeacType returnType;

    public WeacFunctionCallInsn(String name, WeacType owner, int nArgs, boolean instanceInStack, WeacType[] argTypes, WeacType returnType) {
        super(FUNCTION_CALL);
        this.name = name;
        this.owner = owner;
        this.nArgs = nArgs;
        this.instanceInStack = instanceInStack;
        this.argTypes = argTypes;
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public int getArgCount() {
        return nArgs;
    }

    public boolean isInstanceInStack() {
        return instanceInStack;
    }

    @Override
    public String toString() {
        return super.toString()+" "+owner.getIdentifier()+" "+name+" "+nArgs+" ("+instanceInStack+")";
    }

    public WeacType getOwner() {
        return owner;
    }

    public WeacType getReturnType() {
        return returnType;
    }

    public WeacType[] getArgTypes() {
        return argTypes;
    }
}
