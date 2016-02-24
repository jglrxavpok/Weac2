package weac.compiler.precompile.insn;

public class LoadBooleanConstant extends PrecompiledInsn {
    private final boolean value;

    public LoadBooleanConstant(boolean value) {
        super(LOAD_BOOLEAN_CONSTANT);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return super.toString()+" "+value;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj))
            return false;
        if(obj instanceof LoadBooleanConstant) {
            LoadBooleanConstant casted = ((LoadBooleanConstant) obj);
            return casted.getValue() == value;
        }
        return false;
    }
}
