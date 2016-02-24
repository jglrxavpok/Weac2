package weac.compiler.precompile.insn;

public class LoadVariable extends PrecompiledInsn implements PrecompileOpcodes {
    private final String name;

    public LoadVariable(String content) {
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
        if(obj instanceof PrecompiledInsn) {
            LoadVariable casted = ((LoadVariable) obj);
            return casted.getName().equals(name);
        }
        return false;
    }
}
