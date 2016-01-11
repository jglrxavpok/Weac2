package org.jglr.weac.precompile.insn;

import org.jglr.weac.utils.EnumOperators;

public class WeacOperatorInsn extends WeacPrecompiledInsn {
    private final EnumOperators operator;
    private final boolean unary;

    public WeacOperatorInsn(EnumOperators operator) {
        super(operator.unary() ? UNARY_OPERATOR : BINARY_OPERATOR);
        this.operator = operator;
        this.unary = operator.unary();
    }

    public boolean isUnary() {
        return unary;
    }

    public EnumOperators getOperator() {
        return operator;
    }
}
