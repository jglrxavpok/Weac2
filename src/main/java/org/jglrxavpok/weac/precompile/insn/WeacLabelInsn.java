package org.jglrxavpok.weac.precompile.insn;

import org.jglrxavpok.weac.precompile.WeacLabel;

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
}
