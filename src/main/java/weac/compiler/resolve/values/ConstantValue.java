package weac.compiler.resolve.values;

import weac.compiler.utils.WeacType;

public class ConstantValue extends Value {
    public ConstantValue(WeacType type) {
        super(type);
        if(type.equals(WeacType.JOBJECT_TYPE))
            new Exception().printStackTrace();
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
