package org.jglrxavpok.weac.precompile.insn;

public class WeacInstanciateInsn extends WeacPrecompiledInsn {
    private final String typeName;

    public WeacInstanciateInsn(String typeName) {
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
}
