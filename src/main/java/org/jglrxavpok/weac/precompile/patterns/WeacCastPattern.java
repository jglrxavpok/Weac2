package org.jglrxavpok.weac.precompile.patterns;

import org.jglrxavpok.weac.precompile.WeacToken;
import org.jglrxavpok.weac.precompile.WeacTokenType;

import java.util.List;

public class WeacCastPattern extends WeacTokenPattern {
    @Override
    public WeacTokenType[] getCategories() {
        return new WeacTokenType[] {
                WeacTokenType.OPENING_PARENTHESIS, WeacTokenType.VARIABLE, WeacTokenType.CLOSING_PARENTHESIS, WeacTokenType.VARIABLE
        };
    }

    @Override
    public void output(List<WeacToken> original, int i, List<WeacToken> output) {
        output.add(original.get(i+3));
        output.add(new WeacToken(original.get(i+1).getContent(), WeacTokenType.CAST, 0));
    }
}
