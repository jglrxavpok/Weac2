package weac.compiler.precompile;

public class CommentToken extends Token {
    private final boolean multiline;

    public CommentToken(String comment, boolean multiline) {
        super(comment, TokenType.COMMENT, comment.length());
        this.multiline = multiline;
    }

    public boolean isMultiline() {
        return multiline;
    }
}
