package org.jglrxavpok.weac.precompile.insn;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj))
            return false;
        if(obj instanceof WeacPrecompiledInsn) {
            WeacLoadStringConstant casted = ((WeacLoadStringConstant) obj);
            return Objects.equals(casted.getValue(), value);
        }
        return false;
    }
}
