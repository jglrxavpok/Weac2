package weac.compiler.precompile.insn;

import weac.compiler.precompile.Label;

public class LabelInsn extends PrecompiledInsn {
    private final Label label;

    private static int labelIndex;

    public LabelInsn(Label label) {
        super(LABEL);
        this.label = label;
    }

    public LabelInsn() {
        super(LABEL);
        this.label = new Label(labelIndex++);
    }

    public Label getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj))
            return false;
        if(obj instanceof PrecompiledInsn) {
            LabelInsn casted = ((LabelInsn) obj);
            return casted.getLabel().equals(label);
        }
        return false;
    }
}
