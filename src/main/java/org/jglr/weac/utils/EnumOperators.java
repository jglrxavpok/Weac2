package org.jglr.weac.utils;

import static org.jglr.weac.utils.EnumOperators.Associativity.*;

public enum EnumOperators {

    RETURN("return", 2, RIGHT),
    NEW("new", 2, RIGHT),
    INCREMENT("++", 2, RIGHT),
    DECREMENT("--", 2, RIGHT),
    BITWISE_NOT("~", 2, RIGHT),
    LOGICAL_NOT("!", 2, RIGHT),
    MULTIPLY("*", 3, LEFT),
    DIVIDE("/", 3, LEFT),
    MOD("%", 3, LEFT),

    UNARY_PLUS("+", 2, RIGHT),
    UNARY_MINUS("-", 2, RIGHT),

    PLUS("+", 4, LEFT),
    MINUS("-", 4, LEFT),
    LEFT_SHIFT("<<", 5, LEFT),
    SIGNED_RIGHT_SHIFT(">>", 5, LEFT),
    UNSIGNED_RIGHT_SHIFT(">>>", 5, LEFT),
    LESS_THAN("<", 6, LEFT),
    LESS_OR_EQUAL("<=", 6, LEFT),
    GREATER_THAN(">", 6, LEFT),
    GREATER_OR_EQUAL(">=", 6, LEFT),
    INSTANCEOF("instanceof", 6, LEFT), // TODO: change away from the Java's instanceof
    EQUAL("==", 7, LEFT),
    NOTEQUAL("!=", 7, LEFT),
    AND("&", 8, LEFT),
    OR("|", 10, LEFT),
    XOR("^", 11, LEFT),
    DOUBLE_AND("&&", 11, LEFT),
    INTERVAL_SEPARATOR("..", 11, LEFT),
    DOUBLE_OR("||", 12, LEFT),

    // ASSIGNMENTS
    SET_TO("=", 14, RIGHT),
    INCREMENT_BY("+=", 14, RIGHT),
    DECREMENT_BY("-=", 14, RIGHT),
    MULTIPLY_BY("*=", 14, RIGHT),
    DIVIDE_BY("/=", 14, RIGHT),
    SET_MODULO("%=", 14, RIGHT),
    APPLY_AND("&=", 14, RIGHT),
    APPLY_XOR("^=", 14, RIGHT),
    APPLY_OR("|=", 14, RIGHT),
    APPLY_LSH("<<=", 14, RIGHT),
    APPLY_RSH(">>=", 14, RIGHT),
    APPLY_URSH(">>>=", 14, RIGHT),
    ;

    private final String raw;
    private final int precedence;
    private final Associativity associativity;

    EnumOperators(String raw, int precedence, Associativity associativity) {
        this.associativity = associativity;
        this.raw = raw;
        this.precedence = precedence;
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

    public boolean unary() {
        return this == UNARY_MINUS || this == UNARY_PLUS || this == NEW;
    }

    public static EnumOperators get(String raw, boolean unary) {
        if(unary) {
            if(raw.equals("+")) {
                return UNARY_PLUS;
            } else if(raw.equals("-")) {
                return UNARY_MINUS;
            }
        }
        for(EnumOperators op : values()) {
            if(op.raw().equals(raw)) {
                return op;
            }
        }
        return null;
    }

    public enum Associativity {
        LEFT, RIGHT
    }

}
