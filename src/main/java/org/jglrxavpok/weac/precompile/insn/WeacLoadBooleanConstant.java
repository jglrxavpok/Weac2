package org.jglrxavpok.weac.precompile.insn;

public class WeacLoadBooleanConstant extends WeacPrecompiledInsn {
    private final boolean value;

    public WeacLoadBooleanConstant(boolean value) {
        super(LOAD_BOOLEAN_CONSTANT);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return super.toString()+" "+value;
    }
}
