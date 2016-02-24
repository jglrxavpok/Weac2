package weac.compiler.precompile.patterns;

import weac.compiler.precompile.NewLocalToken;
import weac.compiler.precompile.Token;
import weac.compiler.precompile.TokenType;
import weac.compiler.utils.EnumOperators;

import java.util.List;

public class LocalCreationPattern extends TokenPattern {
    @Override
    public TokenType[] getCategories() {
        return new TokenType[] {
                TokenType.VARIABLE, TokenType.VARIABLE
        };
    }

    @Override
    public void output(List<Token> original, int i, List<Token> output) {
        Token type = original.get(i);
        Token name = original.get(i+1);
        output.add(new NewLocalToken(type.getContent(), name.getContent()));
        if(original.size()-i > 0) {
            Token potentialOperator = original.get(i+2);
            if(potentialOperator.getType() == TokenType.BINARY_OPERATOR) {
                EnumOperators operator = EnumOperators.get(potentialOperator.getContent());
                if(operator.isVariableAssign()) {
                    output.add(new Token(name.getContent(), TokenType.VARIABLE, -1));
                }
            }
        }
    }
}
