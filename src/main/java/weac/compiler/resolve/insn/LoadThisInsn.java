package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class LoadThisInsn extends LoadVariableInsn {
    public LoadThisInsn(WeacType type) {
        super(0, type);
    }

    @Override
    public String toString() {
        return super.toString()+" this";
    }
}
