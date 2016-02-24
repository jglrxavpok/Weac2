package weac.compiler.precompile.insn;

import weac.compiler.utils.Instruction;

import static weac.compiler.precompile.insn.PrecompileOpcodes.*;

public class PrecompiledInsn implements PrecompileOpcodes, Instruction {

    private final int opcode;

    public PrecompiledInsn(int opcode) {
        this.opcode = opcode;
    }

    public int getOpcode() {
        return opcode;
    }

    @Override
    public String toString() {
        return getName(opcode);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PrecompiledInsn) {
            return ((PrecompiledInsn) obj).getOpcode() == opcode;
        }
        return false;
    }
}
