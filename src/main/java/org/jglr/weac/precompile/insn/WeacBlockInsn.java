package org.jglr.weac.precompile.insn;

import org.jglr.weac.precompile.WeacCodeBlock;

public class WeacBlockInsn extends WeacPrecompiledInsn {
    private final WeacCodeBlock codeBlock;

    public WeacBlockInsn(WeacCodeBlock codeBlock) {
        super(NULL); // Not a really instruction
        this.codeBlock = codeBlock;
    }

    public WeacCodeBlock getCodeBlock() {
        return codeBlock;
    }
}
