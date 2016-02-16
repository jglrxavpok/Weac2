package org.jglrxavpok.weac.precompile.insn;

import java.util.Objects;

public class WeacLoadCharacterConstant extends WeacPrecompiledInsn implements PrecompileOpcodes {
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

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj))
            return false;
        if(obj instanceof WeacLoadCharacterConstant) {
            WeacLoadCharacterConstant casted = ((WeacLoadCharacterConstant) obj);
            return Objects.equals(casted.getValue(), value);
        }
        return false;
    }
}
