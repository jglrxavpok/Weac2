package weac.compiler.precompile.insn;

import weac.compiler.utils.EnumOperators;

public class WeacOperatorInsn extends WeacPrecompiledInsn {
    private final EnumOperators operator;
    private final boolean unary;

    public WeacOperatorInsn(EnumOperators operator) {
        super(operator.isUnary() ? UNARY_OPERATOR : BINARY_OPERATOR);
        this.operator = operator;
        this.unary = operator.isUnary();
    }

    public boolean isUnary() {
        return unary;
    }

    public EnumOperators getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return super.toString() + " "+ getOperator().name();
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj))
            return false;
        if(obj instanceof WeacOperatorInsn) {
            WeacOperatorInsn casted = ((WeacOperatorInsn) obj);
            return casted.getOperator() == operator && casted.isUnary() == unary;
        }
        return false;
    }
}
