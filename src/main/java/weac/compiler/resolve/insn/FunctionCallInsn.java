package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class FunctionCallInsn extends ResolvedInsn {
    private final String name;
    private final WeacType owner;
    private final int nArgs;
    private final WeacType[] argTypes;
    private final WeacType returnType;
    private final boolean isStatic;

    public FunctionCallInsn(String name, WeacType owner, WeacType[] argTypes, WeacType returnType, boolean isStatic) {
        super(FUNCTION_CALL);
        this.name = name;
        this.owner = owner;
        this.nArgs = argTypes.length;
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


    @Override
    public String toString() {
        return super.toString()+" "+owner.getIdentifier()+" "+name+" "+nArgs+" (static: "+isStatic+")";
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
