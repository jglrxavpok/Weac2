package org.jglr.weac.precompile.insn;

public class WeacLoadVariable extends WeacPrecompiledInsn implements Opcodes {
    private final String name;

    public WeacLoadVariable(String content) {
        super(LOAD_VARIABLE);
        this.name = content;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return super.toString()+" "+name;
    }
}
