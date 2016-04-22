package weac.compiler.resolve.insn;

import weac.compiler.precompile.Label;

public class IfNotNullJumpInsn extends JumpInsn {
    public IfNotNullJumpInsn(Label destination) {
        super(IF_NOT_NULL, destination);
    }
}
