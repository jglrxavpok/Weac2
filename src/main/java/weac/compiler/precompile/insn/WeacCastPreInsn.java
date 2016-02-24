package weac.compiler.precompile.insn;

public class WeacCastPreInsn extends WeacPrecompiledInsn {
    private final String type;

    public WeacCastPreInsn(String type) {
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
        if(obj instanceof WeacPrecompiledInsn) {
            return ((WeacCastPreInsn) obj).getType().equals(type);
        }
        return false;
    }
}
