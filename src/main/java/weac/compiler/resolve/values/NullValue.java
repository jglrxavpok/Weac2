package weac.compiler.resolve.values;

import weac.compiler.targets.jvm.JVMWeacTypes;

public class NullValue extends Value {
    public NullValue() {
        super(JVMWeacTypes.NULL_TYPE);
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
