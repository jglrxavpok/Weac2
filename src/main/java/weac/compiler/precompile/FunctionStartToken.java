package weac.compiler.precompile;

public class FunctionStartToken extends Token {
    private final TokenType type;
    private final Token funcToken;

    public FunctionStartToken(TokenType type, Token funcToken) {
        super("", TokenType.FUNCTION_START, 0);
        this.type = type;
        this.funcToken = funcToken;
    }

    public TokenType getFunctionType() {
        return type;
    }

    public Token getFuncToken() {
        return funcToken;
    }
}
