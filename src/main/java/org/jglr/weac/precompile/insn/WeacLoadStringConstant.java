package org.jglr.weac.precompile.insn;

public class WeacLoadStringConstant extends WeacPrecompiledInsn implements Opcodes {
    private final String value;

    public WeacLoadStringConstant(String content) {
        super(LOAD_STRING_CONSTANT);
        this.value = content;
    }

    public String getValue() {
        return value;
    }
}
