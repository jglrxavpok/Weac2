package org.jglr.weac.resolve.insn;

import org.jglr.weac.utils.WeacInsn;

public class WeacResolvedInsn implements ResolveOpcodes, WeacInsn {

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
