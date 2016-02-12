package org.jglrxavpok.weac.resolve.values;

import org.jglrxavpok.weac.utils.WeacType;

public abstract class WeacValue {

    private final WeacType type;

    public WeacValue(WeacType type) {
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
        return "WeacValue["+getName()+"]";
    }
}
