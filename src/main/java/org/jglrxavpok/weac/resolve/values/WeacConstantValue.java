package org.jglrxavpok.weac.resolve.values;

import org.jglrxavpok.weac.utils.WeacType;

public class WeacConstantValue extends WeacValue {
    public WeacConstantValue(WeacType type) {
        super(type);
    }

    @Override
    public String getName() {
        return "constant";
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public boolean isVariable() {
        return false;
    }

    @Override
    public boolean isField() {
        return false;
    }

    @Override
    public int getLocalVariableIndex() {
        return -1;
    }
}
