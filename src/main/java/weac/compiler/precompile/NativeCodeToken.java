package weac.compiler.precompile;

public class NativeCodeToken extends Token {

    public NativeCodeToken(String nativeCode, TokenType tokenType, int length) {
        super(nativeCode, tokenType, length);
    }

    public String getCode() {
        return getContent();
    }
}
