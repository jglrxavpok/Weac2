package org.jglrxavpok.weac.precompile.insn;

import org.jglrxavpok.weac.utils.WeacInsn;

public class WeacPrecompiledInsn implements PrecompileOpcodes, WeacInsn {

    private final int opcode;

    public WeacPrecompiledInsn(int opcode) {
        this.opcode = opcode;
    }

    public int getOpcode() {
        return opcode;
    }

    @Override
    public String toString() {
        return PrecompileOpcodes.getName(opcode);
    }
}