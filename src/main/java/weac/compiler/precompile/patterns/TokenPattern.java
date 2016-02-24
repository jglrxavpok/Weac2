package weac.compiler.precompile.patterns;

import weac.compiler.patterns.Pattern;
import weac.compiler.precompile.Token;
import weac.compiler.precompile.TokenType;

public abstract class TokenPattern extends Pattern<Token, TokenType> {

    @Override
    protected boolean isValid(Token insn, TokenType expected, int index) {
        return insn.getType() == expected;
    }

}
