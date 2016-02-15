package org.jglrxavpok.weac.patterns;

import org.jglrxavpok.weac.precompile.insn.WeacPrecompiledInsn;
import org.jglrxavpok.weac.utils.WeacInsn;

import java.util.List;

public abstract class WeacInstructionPattern<InsnType extends WeacInsn> extends WeacPattern<InsnType, Integer> {

    public abstract int[] getOpcodes();

    @Override
    public Integer[] getCategories() {
        int[] ops = getOpcodes();
        Integer[] categories = new Integer[ops.length];
        for(int i = 0;i<categories.length;i++) {
            categories[i] = ops[i];
        }
        return categories;
    }

    protected boolean isValid(InsnType insn, Integer expectedCode, int index) {
        return insn.getOpcode() == expectedCode;
    }
}
