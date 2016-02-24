package weac.compiler.precompile.insn;

public class NewLocalVar extends PrecompiledInsn {
    private final String type;
    private final String name;

    public NewLocalVar(String type, String name) {
        super(NEW_LOCAL);
        this.type = type;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString()+" "+type+" "+name;
    }
}
