package weac.compiler.precompile.insn;

import weac.compiler.precompile.Label;

public class LabelInsn extends PrecompiledInsn {
    private final Label label;

    private static int labelIndex;

    public LabelInsn(Label label) {
        super(LABEL);
        this.label = label;
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

    @Override
    public String toString() {
        return super.toString()+" "+label;
    }
}
