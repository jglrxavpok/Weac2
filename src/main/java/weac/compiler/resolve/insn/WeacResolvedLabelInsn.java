package weac.compiler.resolve.insn;

import weac.compiler.precompile.WeacLabel;

public class WeacResolvedLabelInsn extends WeacResolvedInsn {
    private final WeacLabel label;

    public WeacResolvedLabelInsn(WeacLabel label) {
        super(LABEL);
        this.label = label;
    }

    public WeacLabel getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return super.toString()+" "+label;
    }
}
