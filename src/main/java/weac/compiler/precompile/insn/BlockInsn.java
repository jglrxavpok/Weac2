package weac.compiler.precompile.insn;

import weac.compiler.precompile.CodeBlock;

public class BlockInsn extends PrecompiledInsn {
    private final CodeBlock codeBlock;

    public BlockInsn(CodeBlock codeBlock) {
        super(NULL); // Not a really instruction
        this.codeBlock = codeBlock;
    }

    public CodeBlock getCodeBlock() {
        return codeBlock;
    }
}
