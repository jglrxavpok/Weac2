package weac.compiler.precompile;

public class SpaceToken extends WeacToken {
    public SpaceToken() {
        super(" ", WeacTokenType.WAITING_FOR_NEXT, 1);
    }
}
