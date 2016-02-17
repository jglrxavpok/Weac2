package org.jglrxavpok.weac.precompile.insn;

public class WeacNewLocalVar extends WeacPrecompiledInsn {
    private final String type;
    private final String name;

    public WeacNewLocalVar(String type, String name) {
        super(PrecompileOpcodes.NEW_LOCAL);
        this.type = type;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString()+" "+type+" "+name;
    }
}
