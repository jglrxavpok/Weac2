package weac.compiler.precompile.insn;

import weac.compiler.precompile.WeacLabel;

public class WeacLabelInsn extends WeacPrecompiledInsn {
    private final WeacLabel label;

    private static int labelIndex;

    public WeacLabelInsn(WeacLabel label) {
        super(LABEL);
        this.label = label;
    }

    public WeacLabelInsn() {
        super(LABEL);
        this.label = new WeacLabel(labelIndex++);
    }

    public WeacLabel getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj))
            return false;
        if(obj instanceof WeacPrecompiledInsn) {
            WeacLabelInsn casted = ((WeacLabelInsn) obj);
            return casted.getLabel().equals(label);
        }
        return false;
    }
}
