package weac.compiler.resolve.values;

import weac.compiler.utils.WeacType;

public abstract class Value {

    private final WeacType type;

    public Value(WeacType type) {
        this.type = type;
    }

    public WeacType getType() {
        return type;
    }

    public abstract String getName();

    public abstract boolean isConstant();

    public abstract boolean isVariable();

    public abstract boolean isField();

    public abstract int getLocalVariableIndex();

    @Override
    public String toString() {
        return "Value["+getName()+" ("+getType()+")]";
    }
}
