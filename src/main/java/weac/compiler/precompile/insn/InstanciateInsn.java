package weac.compiler.precompile.insn;

public class InstanciateInsn extends PrecompiledInsn {
    private final String typeName;

    public InstanciateInsn(String typeName) {
        super(NEW);
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    @Override
    public String toString() {
        return super.toString()+" "+typeName;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj))
            return false;
        if(obj instanceof PrecompiledInsn) {
            InstanciateInsn casted = ((InstanciateInsn) obj);
            return casted.getTypeName().equals(typeName);
        }
        return false;
    }
}
