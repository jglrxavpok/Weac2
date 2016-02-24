package weac.compiler.precompile.patterns;

import weac.compiler.precompile.Token;
import weac.compiler.precompile.TokenType;

import java.util.List;

public class CastPattern extends TokenPattern {
    @Override
    public TokenType[] getCategories() {
        return new TokenType[] {
                TokenType.OPENING_PARENTHESIS, TokenType.VARIABLE, TokenType.CLOSING_PARENTHESIS
        };
    }

    @Override
    public boolean matches(List<Token> insns, int index) {
        if(!super.matches(insns, index)) {
            return false;
        }
        if(insns.size() > index+3) {
            Token token = insns.get(index + 3);
            return token.getType().isValue() || token.getType() == TokenType.FUNCTION;
        } else {
            return false;
        }
    }

    @Override
    public int consumeCount(List<Token> insns, int index) {
        return super.consumeCount(insns, index) + 1;
    }

    @Override
    public void output(List<Token> original, int i, List<Token> output) {
        output.add(new Token(original.get(i+1).getContent(), TokenType.CAST, 0));
        output.add(original.get(i+3));
    }
}
