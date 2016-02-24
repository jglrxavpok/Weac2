package weac.compiler.precompile.patterns;

import weac.compiler.precompile.WeacToken;
import weac.compiler.precompile.WeacTokenType;

import java.util.List;

public class WeacElseIfPattern extends WeacTokenPattern {
    @Override
    public WeacTokenType[] getCategories() {
        return new WeacTokenType[] {
                WeacTokenType.ELSE, WeacTokenType.IF
        };
    }

    @Override
    public void output(List<WeacToken> original, int i, List<WeacToken> output) {
        output.add(new WeacToken("else if", WeacTokenType.ELSEIF, 0));
    }
}
