package org.jglr.weac.utils;

public enum EnumOperators {

    INCREMENT("++", 2),
    DECREMENT("--", 2),
    BITWISE_NOT("~", 2),
    LOGICAL_NOT("!", 2),
    MULTIPLY("*", 3),
    DIVIDE("/", 3),
    MOD("%", 3),
    PLUS("+", 4),
    MINOR("-", 4),
    LEFT_SHIFT("<<", 5),
    SIGNED_RIGHT_SHIFT(">>", 5),
    UNSIGNED_RIGHT_SHIFT(">>>", 5),
    LESS_THAN("<", 6),
    LESS_OR_EQUAL("<=", 6),
    GREATER_THAN(">", 6),
    GREATER_OR_EQUAL(">=", 6),
    INSTANCEOF("instanceof", 6), // TODO: change away from the Java's instanceof
    EQUAL("==", 7),
    NOTEQUAL("!=", 7),
    AND("&", 8),
    OR("|", 10),
    XOR("^", 11),
    DOUBLE_AND("&", 11),
    DOUBLE_OR("||", 12)
    ;

    private final String raw;
    private final int precedence;

    EnumOperators(String raw, int precedence) {
        this.raw = raw;
        this.precedence = precedence;
    }

    public String raw() {
        return raw;
    }

    public int precedence() {
        return precedence;
    }


}
