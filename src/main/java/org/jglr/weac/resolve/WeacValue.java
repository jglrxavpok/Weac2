package org.jglr.weac.resolve;

import org.jglr.weac.utils.WeacType;

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

    public boolean isVariable() {
        return !isConstant();
    }

    public abstract int getLocalVariableIndex();

    @Override
    public String toString() {
        return "WeacValue["+getName()+"]";
    }
}
