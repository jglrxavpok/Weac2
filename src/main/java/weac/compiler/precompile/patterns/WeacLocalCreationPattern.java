package weac.compiler.precompile.patterns;

import weac.compiler.precompile.WeacNewLocalToken;
import weac.compiler.precompile.WeacToken;
import weac.compiler.precompile.WeacTokenType;
import weac.compiler.utils.EnumOperators;

import java.util.List;

public class WeacLocalCreationPattern extends WeacTokenPattern {
    @Override
    public WeacTokenType[] getCategories() {
        return new WeacTokenType[] {
                WeacTokenType.VARIABLE, WeacTokenType.VARIABLE
        };
    }

    @Override
    public void output(List<WeacToken> original, int i, List<WeacToken> output) {
        WeacToken type = original.get(i);
        WeacToken name = original.get(i+1);
        output.add(new WeacNewLocalToken(type.getContent(), name.getContent()));
        if(original.size()-i > 0) {
            WeacToken potentialOperator = original.get(i+2);
            if(potentialOperator.getType() == WeacTokenType.BINARY_OPERATOR) {
                EnumOperators operator = EnumOperators.get(potentialOperator.getContent());
                if(operator.isVariableAssign()) {
                    output.add(new WeacToken(name.getContent(), WeacTokenType.VARIABLE, -1));
                }
            }
        }
    }
}
