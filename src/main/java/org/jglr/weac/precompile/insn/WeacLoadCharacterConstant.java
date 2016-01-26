package org.jglr.weac.precompile.insn;

public class WeacLoadCharacterConstant extends WeacPrecompiledInsn implements Opcodes {
    private final String value;

    public WeacLoadCharacterConstant(String content) {
        super(LOAD_CHARACTER_CONSTANT);
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
