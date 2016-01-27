package org.jglr.weac.resolve.insn;

public class WeacResolvedInsn implements ResolveOpcodes {

    private final int opcode;

    public WeacResolvedInsn(int opcode) {
        this.opcode = opcode;
    }

    public int getOpcode() {
        return opcode;
    }

    @Override
    public String toString() {
        return ResolveOpcodes.getName(opcode);
    }
}
