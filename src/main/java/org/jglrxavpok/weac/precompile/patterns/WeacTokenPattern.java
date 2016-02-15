package org.jglrxavpok.weac.precompile.patterns;

import org.jglrxavpok.weac.patterns.WeacPattern;
import org.jglrxavpok.weac.precompile.WeacToken;
import org.jglrxavpok.weac.precompile.WeacTokenType;
import org.jglrxavpok.weac.precompile.insn.WeacPrecompiledInsn;

import java.util.List;

public abstract class WeacTokenPattern extends WeacPattern<WeacToken, WeacTokenType> {

    @Override
    protected boolean isValid(WeacToken insn, WeacTokenType expected, int index) {
        return insn.getType() == expected;
    }

}
