package org.jglrxavpok.weac.precompile;

public enum WeacTokenType {

    WAITING_FOR_NEXT,
    NUMBER(true),
    SINGLE_CHARACTER(true),
    STRING(true),
    LITERAL,
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

    FUNCTION_START,

    // Those two are created from the OPERATOR value after analysing their context (parenthesis before/after it or not)
    BINARY_OPERATOR,

    UNARY_OPERATOR;

    private final boolean isValue;

    WeacTokenType() {
        this(false);
    }

    WeacTokenType(boolean isValue) {
        this.isValue = isValue;
    }

    public boolean isValue() {
        return isValue;
    }
}
