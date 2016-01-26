package org.jglr.weac.precompile.patterns;

import org.jglr.weac.precompile.insn.Opcodes;
import org.jglr.weac.precompile.insn.WeacPrecompiledInsn;

import java.util.List;

public abstract class WeacInstructionPattern implements Opcodes {

    public abstract int[] getOpcodes();

    public boolean matches(List<WeacPrecompiledInsn> insns, int index) {
        int localIndex = 0;
        for(int i = index;i<insns.size() && localIndex < getOpcodes().length; i++, localIndex++) {
            WeacPrecompiledInsn insn = insns.get(i);
            int expectedCode = getOpcodes()[localIndex];
            if(!isValid(insn, expectedCode, localIndex)) {
                return false;
            }
        }
        return localIndex == getOpcodes().length;
    }

    public int consumeCount(List<WeacPrecompiledInsn> insns, int index) {
        return getOpcodes().length;
    }

    protected boolean isValid(WeacPrecompiledInsn insn, int expectedCode, int index) {
        return insn.getOpcode() == expectedCode;
    }

    public abstract void output(List<WeacPrecompiledInsn> original, int i, List<WeacPrecompiledInsn> output);
}
