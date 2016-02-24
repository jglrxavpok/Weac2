package weac.compiler.precompile.insn;

import java.util.Objects;

public class LoadCharacterConstant extends PrecompiledInsn implements PrecompileOpcodes {
    private final String value;

    public LoadCharacterConstant(String content) {
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
        if(obj instanceof LoadCharacterConstant) {
            LoadCharacterConstant casted = ((LoadCharacterConstant) obj);
            return Objects.equals(casted.getValue(), value);
        }
        return false;
    }
}
