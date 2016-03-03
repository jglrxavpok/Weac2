package weac.compiler.resolve.insn;

import weac.compiler.precompile.Label;

public class GotoResInsn extends ResolvedInsn {
    private final Label label;

    public GotoResInsn(Label label) {
        super(JUMP);
        this.label = label;
    }

    public Label getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return super.toString()+" "+label;
    }
}
