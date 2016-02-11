package org.jglrxavpok.weac.resolve;

import org.jglrxavpok.weac.utils.WeacType;

public class WeacVariableValue extends WeacValue {
    private final String name;
    private final WeacType type;
    private final int id;

    public WeacVariableValue(String name, WeacType type, int id) {
        super(type);
        this.name = name;
        this.type = type;
        this.id = id;
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
        return true;
    }

    @Override
    public boolean isField() {
        return false;
    }

    @Override
    public int getLocalVariableIndex() {
        return id;
    }
}
