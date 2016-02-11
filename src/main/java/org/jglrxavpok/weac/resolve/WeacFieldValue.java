package org.jglrxavpok.weac.resolve;

import org.jglrxavpok.weac.utils.WeacType;

public class WeacFieldValue extends WeacValue {
    private final String name;
    private final WeacType owner;

    public WeacFieldValue(String name, WeacType owner, WeacType type) {
        super(type);
        this.name = name;
        this.owner = owner;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean isVariable() {
        return false;
    }

    @Override
    public boolean isField() {
        return true;
    }

    @Override
    public int getLocalVariableIndex() {
        return -1;
    }

    public WeacType getOwner() {
        return owner;
    }
}
