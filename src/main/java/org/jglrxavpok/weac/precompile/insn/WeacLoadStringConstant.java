package org.jglrxavpok.weac.precompile.insn;

public class WeacLoadStringConstant extends WeacPrecompiledInsn implements PrecompileOpcodes {
    private final String value;

    public WeacLoadStringConstant(String content) {
        super(LOAD_STRING_CONSTANT);
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
