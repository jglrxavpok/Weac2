package weac.compiler.precompile.insn;

import weac.compiler.utils.WeacInsn;

import static weac.compiler.precompile.insn.PrecompileOpcodes.*;

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
        return getName(opcode);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WeacPrecompiledInsn) {
            return ((WeacPrecompiledInsn) obj).getOpcode() == opcode;
        }
        return false;
    }
}
