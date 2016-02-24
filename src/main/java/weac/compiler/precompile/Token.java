package weac.compiler.precompile;

public class Token {

    private final String content;
    private TokenType type;
    public final int length;

    public Token(String content, TokenType type, int length) {
        this.content = content;
        this.length = length;
        this.type = type;
    }

    public TokenType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public boolean isClosingBracketLike() {
        return type == TokenType.CLOSING_PARENTHESIS || type == TokenType.CLOSING_SQUARE_BRACKETS || type == TokenType.CLOSING_CURLY_BRACKETS;
    }

    public boolean isOpeningBracketLike() {
        return type == TokenType.OPENING_PARENTHESIS || type == TokenType.OPENING_SQUARE_BRACKETS || type == TokenType.OPENING_CURLY_BRACKETS;
    }

    public boolean isOpposite(Token token) {
        switch (token.getType()) {
            case OPENING_PARENTHESIS:
                return this.getType() == TokenType.CLOSING_PARENTHESIS;

            case CLOSING_PARENTHESIS:
                return this.getType() == TokenType.OPENING_PARENTHESIS;

            case OPENING_CURLY_BRACKETS:
                return this.getType() == TokenType.CLOSING_CURLY_BRACKETS;

            case CLOSING_CURLY_BRACKETS:
                return this.getType() == TokenType.OPENING_CURLY_BRACKETS;

            case OPENING_SQUARE_BRACKETS:
                return this.getType() == TokenType.CLOSING_SQUARE_BRACKETS;

            case CLOSING_SQUARE_BRACKETS:
                return this.getType() == TokenType.OPENING_SQUARE_BRACKETS;
        }
        return false;
    }

    @Override
    public String toString() {
        return type+" "+getContent();
    }
}
