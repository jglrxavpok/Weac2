package org.jglr.weac.resolve;

import org.jglr.weac.utils.WeacType;

public class WeacConstantValue extends WeacValue {
    public WeacConstantValue(WeacType type) {
        super(type);
    }

    @Override
    public String getName() {
        return "constant of type "+getType();
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public int getLocalVariableIndex() {
        return -1;
    }
}
