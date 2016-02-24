package weac.compiler.precompile;

public class SpaceToken extends Token {
    public SpaceToken() {
        super(" ", TokenType.WAITING_FOR_NEXT, 1);
    }
}
