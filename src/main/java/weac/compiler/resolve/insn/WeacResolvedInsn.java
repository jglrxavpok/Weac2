package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacInsn;

import static weac.compiler.resolve.insn.ResolveOpcodes.*;

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
        return getName(opcode);
    }
}
