package org.jglrxavpok.weac.precompile.patterns;

import org.jglrxavpok.weac.precompile.WeacToken;
import org.jglrxavpok.weac.precompile.WeacTokenType;

import java.util.List;

public class WeacCastPattern extends WeacTokenPattern {
    @Override
    public WeacTokenType[] getCategories() {
        return new WeacTokenType[] {
                WeacTokenType.OPENING_PARENTHESIS, WeacTokenType.VARIABLE, WeacTokenType.CLOSING_PARENTHESIS
        };
    }

    @Override
    public boolean matches(List<WeacToken> insns, int index) {
        if(!super.matches(insns, index)) {
            return false;
        }
        if(insns.size() > index+3) {
            WeacToken token = insns.get(index + 3);
            return token.getType().isValue() || token.getType() == WeacTokenType.FUNCTION;
        } else {
            return false;
        }
    }

    @Override
    public int consumeCount(List<WeacToken> insns, int index) {
        return super.consumeCount(insns, index) + 1;
    }

    @Override
    public void output(List<WeacToken> original, int i, List<WeacToken> output) {
        output.add(new WeacToken(original.get(i+1).getContent(), WeacTokenType.CAST, 0));
        output.add(original.get(i+3));
    }
}
