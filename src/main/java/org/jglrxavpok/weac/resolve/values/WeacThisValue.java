package org.jglrxavpok.weac.resolve.values;

import org.jglrxavpok.weac.utils.WeacType;

public class WeacThisValue extends WeacValue {

    public WeacThisValue(WeacType currentType) {
        super(currentType);
    }

    @Override
    public String getName() {
        return "this";
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
        return 0;
    }
}
