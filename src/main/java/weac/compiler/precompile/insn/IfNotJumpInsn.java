package weac.compiler.precompile.insn;

import weac.compiler.precompile.Label;

public class IfNotJumpInsn extends PrecompiledInsn {
    private final Label jumpTo;

    public IfNotJumpInsn(Label jumpTo) {
        super(JUMP_IF_NOT_TRUE);
        this.jumpTo = jumpTo;
    }

    public Label getJumpTo() {
        return jumpTo;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj))
            return false;
        if(obj instanceof PrecompiledInsn) {
            IfNotJumpInsn casted = ((IfNotJumpInsn) obj);
            return casted.jumpTo.equals(jumpTo);
        }
        return false;
    }
}
