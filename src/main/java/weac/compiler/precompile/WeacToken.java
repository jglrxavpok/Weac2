package weac.compiler.precompile;

public class WeacToken {

    private final String content;
    private WeacTokenType type;
    public final int length;

    public WeacToken(String content, WeacTokenType type, int length) {
        this.content = content;
        this.length = length;
        this.type = type;
    }

    public WeacTokenType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public void setType(WeacTokenType type) {
        this.type = type;
    }

    public boolean isClosingBracketLike() {
        return type == WeacTokenType.CLOSING_PARENTHESIS || type == WeacTokenType.CLOSING_SQUARE_BRACKETS || type == WeacTokenType.CLOSING_CURLY_BRACKETS;
    }

    public boolean isOpeningBracketLike() {
        return type == WeacTokenType.OPENING_PARENTHESIS || type == WeacTokenType.OPENING_SQUARE_BRACKETS || type == WeacTokenType.OPENING_CURLY_BRACKETS;
    }

    public boolean isOpposite(WeacToken token) {
        switch (token.getType()) {
            case OPENING_PARENTHESIS:
                return this.getType() == WeacTokenType.CLOSING_PARENTHESIS;

            case CLOSING_PARENTHESIS:
                return this.getType() == WeacTokenType.OPENING_PARENTHESIS;

            case OPENING_CURLY_BRACKETS:
                return this.getType() == WeacTokenType.CLOSING_CURLY_BRACKETS;

            case CLOSING_CURLY_BRACKETS:
                return this.getType() == WeacTokenType.OPENING_CURLY_BRACKETS;

            case OPENING_SQUARE_BRACKETS:
                return this.getType() == WeacTokenType.CLOSING_SQUARE_BRACKETS;

            case CLOSING_SQUARE_BRACKETS:
                return this.getType() == WeacTokenType.OPENING_SQUARE_BRACKETS;
        }
        return false;
    }

    @Override
    public String toString() {
        return type+" "+getContent();
    }
}
