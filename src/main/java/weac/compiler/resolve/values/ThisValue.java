package weac.compiler.resolve.values;

import weac.compiler.utils.WeacType;

public class ThisValue extends Value {

    public ThisValue(WeacType currentType) {
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
