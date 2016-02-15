package org.jglrxavpok.weac.precompile.insn;

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
}
