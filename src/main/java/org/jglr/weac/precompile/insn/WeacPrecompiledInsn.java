package org.jglr.weac.precompile.insn;

public class WeacPrecompiledInsn implements Opcodes {

    private final int opcode;

    public WeacPrecompiledInsn(int opcode) {
        this.opcode = opcode;
    }

    public int getOpcode() {
        return opcode;
    }

    @Override
    public String toString() {
        return Opcodes.getName(opcode);
    }
}
