package weac.compiler.precompile.patterns;

import weac.compiler.precompile.Token;
import weac.compiler.precompile.TokenType;

import java.util.List;

public class ElseIfPattern extends TokenPattern {
    @Override
    public TokenType[] getCategories() {
        return new TokenType[] {
                TokenType.ELSE, TokenType.IF
        };
    }

    @Override
    public void output(List<Token> original, int i, List<Token> output) {
        output.add(new Token("else if", TokenType.ELSEIF, 0));
    }
}
