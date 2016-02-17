package org.jglrxavpok.weac.utils;

import static org.jglrxavpok.weac.utils.EnumOperators.Associativity.*;

public enum EnumOperators {

    RETURN("return", 100, RIGHT, true),
    NEW("new", 2, RIGHT, true),
    INCREMENT("++", 2, RIGHT, true),
    DECREMENT("--", 2, RIGHT, true),
    BITWISE_NOT("~", 2, RIGHT, true),
    LOGICAL_NOT("!", 2, RIGHT, true),
    MULTIPLY("*", 3, LEFT, false),
    DIVIDE("/", 3, LEFT, false),
    MOD("%", 3, LEFT, false),

    UNARY_PLUS("+", 2, RIGHT, true),
    UNARY_MINUS("-", 2, RIGHT, true),

    PLUS("+", 4, LEFT, false),
    MINUS("-", 4, LEFT, false),
    LEFT_SHIFT("<<", 5, LEFT, false),
    SIGNED_RIGHT_SHIFT(">>", 5, LEFT, false),
    UNSIGNED_RIGHT_SHIFT(">>>", 5, LEFT, false),
    LESS_THAN("<", 6, LEFT, false),
    LESS_OR_EQUAL("<=", 6, LEFT, false),
    GREATER_THAN(">", 6, LEFT, false),
    GREATER_OR_EQUAL(">=", 6, LEFT, false),
    INSTANCEOF("instanceof", 6, LEFT, false), // TODO: change away from the Java's instanceof
    EQUAL("==", 7, LEFT, false),
    NOTEQUAL("!=", 7, LEFT, false),
    AND("&", 8, LEFT, false),
    OR("|", 10, LEFT, false),
    XOR("^", 11, LEFT, false),
    DOUBLE_AND("&&", 11, LEFT, false),
    INTERVAL_SEPARATOR("..", 11, LEFT, false),
    DOUBLE_OR("||", 12, LEFT, false),

    // ASSIGNMENTS
    SET_TO("=", 14, RIGHT, false),
    INCREMENT_BY("+=", 14, RIGHT, false),
    DECREMENT_BY("-=", 14, RIGHT, false),
    MULTIPLY_BY("*=", 14, RIGHT, false),
    DIVIDE_BY("/=", 14, RIGHT, false),
    SET_MODULO("%=", 14, RIGHT, false),
    APPLY_AND("&=", 14, RIGHT, false),
    APPLY_XOR("^=", 14, RIGHT, false),
    APPLY_OR("|=", 14, RIGHT, false),
    APPLY_LSH("<<=", 14, RIGHT, false),
    APPLY_RSH(">>=", 14, RIGHT, false),
    APPLY_URSH(">>>=", 14, RIGHT, false),
    ;

    private final String raw;
    private final int precedence;
    private final boolean isUnary;
    private final Associativity associativity;

    EnumOperators(String raw, int precedence, Associativity associativity, boolean isUnary) {
        this.associativity = associativity;
        this.raw = raw;
        this.precedence = precedence;
        this.isUnary = isUnary;
    }

    public String raw() {
        return raw;
    }

    public int precedence() {
        return precedence;
    }

    public Associativity associativity() {
        return associativity;
    }

    public static EnumOperators get(String raw) {
        EnumOperators binary = get(raw, false);
        if(binary == null) {
            return get(raw, true);
        }
        return binary;
    }

    public static EnumOperators get(String raw, boolean unary) {
        for(EnumOperators op : values()) {
            if(op.isUnary == unary && op.raw().equals(raw)) {
                return op;
            }
        }
        return null;
    }

    public boolean isUnary() {
        return isUnary;
    }

    public static boolean isAmbiguous(String content) {
        return content.equals("+") || content.equals("-");
    }

    public enum Associativity {
        LEFT, RIGHT
    }

}
