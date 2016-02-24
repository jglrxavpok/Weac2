package weac.compiler.precompile.patterns;

import weac.compiler.patterns.WeacPattern;
import weac.compiler.precompile.WeacToken;
import weac.compiler.precompile.WeacTokenType;

public abstract class WeacTokenPattern extends WeacPattern<WeacToken, WeacTokenType> {

    @Override
    protected boolean isValid(WeacToken insn, WeacTokenType expected, int index) {
        return insn.getType() == expected;
    }

}
