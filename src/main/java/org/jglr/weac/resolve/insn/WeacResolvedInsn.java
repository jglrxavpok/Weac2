package org.jglr.weac.resolve.insn;

import org.jglr.weac.precompile.insn.Opcodes;

public class WeacResolvedInsn implements Opcodes {

    private final int opcode;

    public WeacResolvedInsn(int opcode) {
        this.opcode = opcode;
    }

    public int getOpcode() {
        return opcode;
    }
}
