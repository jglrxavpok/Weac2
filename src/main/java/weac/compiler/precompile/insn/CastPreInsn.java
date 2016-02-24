package weac.compiler.precompile.insn;

public class CastPreInsn extends PrecompiledInsn {
    private final String type;

    public CastPreInsn(String type) {
        super(CAST);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString()+" "+type;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PrecompiledInsn) {
            return ((CastPreInsn) obj).getType().equals(type);
        }
        return false;
    }
}
