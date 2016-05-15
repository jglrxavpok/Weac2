package weac.compiler.precompile;

public class NewLineToken extends Token {
    public NewLineToken() {
        super("\n", TokenType.NEW_LINE, 1);
    }
}
