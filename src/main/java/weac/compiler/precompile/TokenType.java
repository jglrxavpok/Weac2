package weac.compiler.precompile;

public enum TokenType {

    WAITING_FOR_NEXT,
    NUMBER(true),
    SINGLE_CHARACTER(true),
    STRING(true),
    LITERAL(true),
    OPERATOR,
    MEMBER_ACCESSING,
    ARGUMENT_SEPARATOR,

    OPENING_PARENTHESIS,
    CLOSING_PARENTHESIS,

    OPENING_SQUARE_BRACKETS,
    CLOSING_SQUARE_BRACKETS,

    OPENING_CURLY_BRACKETS,
    CLOSING_CURLY_BRACKETS,

    DEFINE_INTERVAL,
    DEFINE_ARRAY, // requires the size to be given inside the token
    INTERVAL_STEP,

    // Those six are created from the LITERAL value after analysing their context (parenthesis after it or not)
    VARIABLE(true),
    FUNCTION,
    BOOLEAN(true),
    THIS(true),
    NULL(true),
    TYPE,

    IF,
    ELSE,
    ELSEIF,

    FUNCTION_START,
    INSTRUCTION_END,

    NEW_LOCAL,

    // Those two are created from the OPERATOR value after analysing their context (parenthesis before/after it or not)
    BINARY_OPERATOR,

    UNARY_OPERATOR,
    CAST,

    POP_INSTANCE,
    ARRAY_START,
    NATIVE_CODE;

    private final boolean isValue;

    TokenType() {
        this(false);
    }

    TokenType(boolean isValue) {
        this.isValue = isValue;
    }

    public boolean isValue() {
        return isValue;
    }
}
