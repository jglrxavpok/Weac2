package org.jglr.weac.resolve.insn;

import org.jglr.weac.precompile.WeacLabel;

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
