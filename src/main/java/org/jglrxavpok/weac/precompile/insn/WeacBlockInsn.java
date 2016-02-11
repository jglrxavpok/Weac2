package org.jglrxavpok.weac.precompile.insn;

import org.jglrxavpok.weac.precompile.WeacCodeBlock;

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
