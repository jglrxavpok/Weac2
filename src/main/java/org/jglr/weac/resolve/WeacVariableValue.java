package org.jglr.weac.resolve;

import org.jglr.weac.utils.WeacType;

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
    public int getLocalVariableIndex() {
        return id;
    }
}
