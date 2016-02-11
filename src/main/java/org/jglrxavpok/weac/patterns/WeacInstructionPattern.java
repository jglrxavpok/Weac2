package org.jglrxavpok.weac.patterns;

import org.jglrxavpok.weac.precompile.insn.WeacPrecompiledInsn;
import org.jglrxavpok.weac.utils.WeacInsn;

import java.util.List;

public abstract class WeacInstructionPattern<InsnType extends WeacInsn> {

    public abstract int[] getOpcodes();

    public boolean matches(List<InsnType> insns, int index) {
        int localIndex = 0;
        for(int i = index;i<insns.size() && localIndex < getOpcodes().length; i++, localIndex++) {
            InsnType insn = insns.get(i);
            int expectedCode = getOpcodes()[localIndex];
            if(!isValid(insn, expectedCode, localIndex)) {
                return false;
            }
        }
        return localIndex == getOpcodes().length;
    }

    public int consumeCount(List<InsnType> insns, int index) {
        return getOpcodes().length;
    }

    protected boolean isValid(InsnType insn, int expectedCode, int index) {
        return insn.getOpcode() == expectedCode;
    }

    public abstract void output(List<InsnType> original, int i, List<WeacPrecompiledInsn> output);
}
