package org.jglr.weac.resolve.insn;

public class WeacLoadThisInsn extends WeacLoadVariableInsn {
    public WeacLoadThisInsn() {
        super(0);
    }

    @Override
    public String toString() {
        return super.toString()+" this";
    }
}
