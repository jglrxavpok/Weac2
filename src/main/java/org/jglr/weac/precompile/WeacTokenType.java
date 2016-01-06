package org.jglr.weac.precompile;

public enum WeacTokenType {

    WAITING_FOR_NEXT,
    NUMBER,
    SINGLE_CHARACTER,
    STRING,
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

    INTERVAL_SEPARATOR,
    INTERVAL_STEP,

    // Those three are created from the LITERAL value after analysing their context (parenthesis after it or not)
    VARIABLE,
    FUNCTION,
    TYPE,

    // Those two are created from the OPERATOR value after analysing their context (parenthesis before/after it or not)
    BINARY_OPERATOR,
    UNARY_OPERATOR

}
