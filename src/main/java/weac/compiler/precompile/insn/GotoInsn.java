package weac.compiler.precompile.insn;

import weac.compiler.precompile.Label;

public class GotoInsn extends PrecompiledInsn {
    private final Label label;

    public GotoInsn(Label label) {
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
