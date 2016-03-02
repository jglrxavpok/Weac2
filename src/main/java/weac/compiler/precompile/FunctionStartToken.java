package weac.compiler.precompile;

public class FunctionStartToken extends Token {
    private final TokenType type;

    public FunctionStartToken(TokenType type) {
        super("", TokenType.FUNCTION_START, 0);
        this.type = type;
    }

    public TokenType getFunctionType() {
        return type;
    }
}
