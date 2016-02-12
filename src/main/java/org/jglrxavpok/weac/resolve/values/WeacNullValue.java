package org.jglrxavpok.weac.resolve.values;

import org.jglrxavpok.weac.utils.WeacType;

public class WeacNullValue extends WeacValue {
    public WeacNullValue() {
        super(WeacType.OBJECT_TYPE);
    }

    @Override
    public String getName() {
        return "null";
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
