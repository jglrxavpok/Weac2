package weac.compiler.resolve.insn;

import weac.compiler.precompile.Label;

public class ResolvedLabelInsn extends ResolvedInsn {
    private final Label label;

    public ResolvedLabelInsn(Label label) {
        super(LABEL);
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
