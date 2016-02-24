package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class FunctionCallInsn extends ResolvedInsn {
    private final String name;
    private final WeacType owner;
    private final int nArgs;
    private final boolean instanceInStack;
    private final WeacType[] argTypes;
    private final WeacType returnType;
    private final boolean isStatic;

    public FunctionCallInsn(String name, WeacType owner, int nArgs, boolean instanceInStack, WeacType[] argTypes, WeacType returnType, boolean isStatic) {
        super(FUNCTION_CALL);
        this.name = name;
        this.owner = owner;
        this.nArgs = nArgs;
        this.instanceInStack = instanceInStack;
        this.argTypes = argTypes;
        this.returnType = returnType;
        this.isStatic = isStatic;
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

    public boolean isStatic() {
        return isStatic;
    }
}
