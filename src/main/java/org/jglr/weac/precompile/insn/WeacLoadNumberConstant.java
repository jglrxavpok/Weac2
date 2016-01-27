package org.jglr.weac.precompile.insn;

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
}
