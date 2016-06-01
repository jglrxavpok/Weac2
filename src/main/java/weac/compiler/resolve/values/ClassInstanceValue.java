package weac.compiler.resolve.values;

import weac.compiler.utils.WeacType;

public class ClassInstanceValue extends Value {
    public ClassInstanceValue(WeacType classType) {
        super(classType);
    }

    @Override
    public String getName() {
        return getType()+".class";
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
