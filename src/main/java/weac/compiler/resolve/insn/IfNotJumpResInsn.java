package weac.compiler.resolve.insn;

import weac.compiler.precompile.Label;

public class IfNotJumpResInsn extends ResolvedInsn {
    private final Label jumpTo;

    public IfNotJumpResInsn(Label jumpTo) {
        super(IF_NOT_TRUE_JUMP);
        this.jumpTo = jumpTo;
    }

    public Label getJumpTo() {
        return jumpTo;
    }

    @Override
    public String toString() {
        return super.toString()+" "+jumpTo;
    }
}
