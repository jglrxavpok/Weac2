package weac.compiler.precompile.insn;

public class WeacLoadNumberConstant extends WeacPrecompiledInsn implements PrecompileOpcodes {
    private final String value;

    public WeacLoadNumberConstant(String content) {
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
        if(obj instanceof WeacLoadNumberConstant) {
            WeacLoadNumberConstant casted = ((WeacLoadNumberConstant) obj);
            return casted.getValue().equals(value);
        }
        return false;
    }
}
