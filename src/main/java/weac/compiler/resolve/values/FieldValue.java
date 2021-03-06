package weac.compiler.resolve.values;

import weac.compiler.utils.WeacType;

public class FieldValue extends Value {
    private final String name;
    private final WeacType owner;

    public FieldValue(String name, WeacType owner, WeacType type) {
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
