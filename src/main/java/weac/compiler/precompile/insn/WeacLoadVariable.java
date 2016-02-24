package weac.compiler.precompile.insn;

public class WeacLoadVariable extends WeacPrecompiledInsn implements PrecompileOpcodes {
    private final String name;

    public WeacLoadVariable(String content) {
        super(LOAD_VARIABLE);
        this.name = content;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return super.toString()+" "+name;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj))
            return false;
        if(obj instanceof WeacPrecompiledInsn) {
            WeacLoadVariable casted = ((WeacLoadVariable) obj);
            return casted.getName().equals(name);
        }
        return false;
    }
}
