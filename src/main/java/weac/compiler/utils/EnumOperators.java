package weac.compiler.utils;

import static weac.compiler.utils.EnumOperators.Associativity.*;

public enum EnumOperators {

    RETURN("return", 100, RIGHT, true, false),
    NEW("new", 2, RIGHT, true, false),
    INCREMENT("++", 2, RIGHT, true, false),
    DECREMENT("--", 2, RIGHT, true, false),
    BITWISE_NOT("~", 2, RIGHT, true, false),
    LOGICAL_NOT("!", 2, RIGHT, true, false),
    MULTIPLY("*", 3, LEFT, false, false),
    DIVIDE("/", 3, LEFT, false, false),
    MOD("%", 3, LEFT, false, false),

    UNARY_PLUS("+", 2, RIGHT, true, false),
    UNARY_MINUS("-", 2, RIGHT, true, false),
    CAST("", 2, RIGHT, true, false),

    PLUS("+", 4, LEFT, false, false),
    MINUS("-", 4, LEFT, false, false),
    LEFT_SHIFT("<<", 5, LEFT, false, false),
    SIGNED_RIGHT_SHIFT(">>", 5, LEFT, false, false),
    UNSIGNED_RIGHT_SHIFT(">>>", 5, LEFT, false, false),
    LESS_THAN("<", 6, LEFT, false, false),
    LESS_OR_EQUAL("<=", 6, LEFT, false, false),
    GREATER_THAN(">", 6, LEFT, false, false),
    GREATER_OR_EQUAL(">=", 6, LEFT, false, false),
    INSTANCEOF("instanceof", 6, LEFT, false, false), // TODO: change away from the Java's instanceof
    EQUAL("==", 7, LEFT, false, false),
    NOTEQUAL("!=", 7, LEFT, false, false),
    AND("&", 8, LEFT, false, false),
    OR("|", 10, LEFT, false, false),
    XOR("^", 11, LEFT, false, false),
    DOUBLE_AND("&&", 11, LEFT, false, false),
    INTERVAL_SEPARATOR("..", 11, LEFT, false, false),
    DOUBLE_OR("||", 12, LEFT, false, false),

    // ASSIGNMENTS
    SET_TO("=", 14, RIGHT, false, true),
    INCREMENT_BY("+=", 14, RIGHT, false, true),
    DECREMENT_BY("-=", 14, RIGHT, false, true),
    MULTIPLY_BY("*=", 14, RIGHT, false, true),
    DIVIDE_BY("/=", 14, RIGHT, false, true),
    SET_MODULO("%=", 14, RIGHT, false, true),
    APPLY_AND("&=", 14, RIGHT, false, true),
    APPLY_XOR("^=", 14, RIGHT, false, true),
    APPLY_OR("|=", 14, RIGHT, false, true),
    APPLY_LSH("<<=", 14, RIGHT, false, true),
    APPLY_RSH(">>=", 14, RIGHT, false, true),
    APPLY_URSH(">>>=", 14, RIGHT, false, true),
    ;

    private final String raw;
    private final int precedence;
    private final boolean isUnary;
    private final Associativity associativity;
    private final boolean isVariableAssign;

    EnumOperators(String raw, int precedence, Associativity associativity, boolean isUnary, boolean isVariableAssign) {
        this.associativity = associativity;
        this.raw = raw;
        this.precedence = precedence;
        this.isUnary = isUnary;
        this.isVariableAssign = isVariableAssign;
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
            if(op == CAST)
                continue; // We skip cast operator as it is found through patterns
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

    public boolean isVariableAssign() {
        return isVariableAssign;
    }

    public enum Associativity {
        LEFT, RIGHT
    }

}
