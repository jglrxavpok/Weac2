package weac.compiler.precompile.insn;

import weac.compiler.precompile.WeacCodeBlock;

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
