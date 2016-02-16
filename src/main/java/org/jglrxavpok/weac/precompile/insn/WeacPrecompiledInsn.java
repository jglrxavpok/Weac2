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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WeacPrecompiledInsn) {
            return ((WeacPrecompiledInsn) obj).getOpcode() == opcode;
        }
        return false;
    }
}
