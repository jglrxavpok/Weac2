package org.jglrxavpok.weac.patterns;

import org.jglrxavpok.weac.precompile.insn.WeacPrecompiledInsn;
import org.jglrxavpok.weac.utils.WeacInsn;

import java.util.List;

public abstract class WeacPattern<InsnType, CategoryType> {

    public abstract CategoryType[] getCategories();

    public boolean matches(List<InsnType> insns, int index) {
        int localIndex = 0;
        for(int i = index;i<insns.size() && localIndex < getCategories().length; i++, localIndex++) {
            InsnType insn = insns.get(i);
            CategoryType expectedCode = getCategories()[localIndex];
            if(!isValid(insn, expectedCode, localIndex)) {
                return false;
            }
        }
        return localIndex == getCategories().length;
    }

    public int consumeCount(List<InsnType> insns, int index) {
        return getCategories().length;
    }

    protected abstract boolean isValid(InsnType insn, CategoryType expectedCode, int index);

    public abstract void output(List<InsnType> original, int i, List<InsnType> output);
}
