package weac.compiler.resolve.insn;

import weac.compiler.utils.Instruction;

import static weac.compiler.resolve.insn.ResolveOpcodes.*;

public class ResolvedInsn implements ResolveOpcodes, Instruction {

    private final int opcode;

    public ResolvedInsn(int opcode) {
        this.opcode = opcode;
    }

    public int getOpcode() {
        return opcode;
    }

    @Override
    public String toString() {
        return getName(opcode);
    }
}
