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

    DEFINE_INTERVAL,
    DEFINE_ARRAY, // requires the size to be given inside the token
    INTERVAL_STEP,

    // Those five are created from the LITERAL value after analysing their context (parenthesis after it or not)
    VARIABLE,
    FUNCTION,
    BOOLEAN,
    THIS,
    TYPE,

    IF,

    // Those two are created from the OPERATOR value after analysing their context (parenthesis before/after it or not)
    BINARY_OPERATOR,
    ELSE, UNARY_OPERATOR

}
