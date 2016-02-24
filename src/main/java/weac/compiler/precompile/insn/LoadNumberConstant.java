package weac.compiler.precompile.insn;

public class LoadNumberConstant extends PrecompiledInsn implements PrecompileOpcodes {
    private final String value;

    public LoadNumberConstant(String content) {
        super(LOAD_NUMBER_CONSTANT);
        this.value = content;
    }

    public String getValue() {
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
        if(obj instanceof LoadNumberConstant) {
            LoadNumberConstant casted = ((LoadNumberConstant) obj);
            return casted.getValue().equals(value);
        }
        return false;
    }
}
