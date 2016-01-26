package org.jglr.weac.precompile.insn;

import org.jglr.weac.precompile.WeacLabel;

public class WeacIfNotJumpInsn extends WeacPrecompiledInsn {
    private final WeacLabel jumpTo;

    public WeacIfNotJumpInsn(WeacLabel jumpTo) {
        super(JUMP_IF_NOT_TRUE);
        this.jumpTo = jumpTo;
    }

    public WeacLabel getJumpTo() {
        return jumpTo;
    }
}
