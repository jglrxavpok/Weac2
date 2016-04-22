package weac.compiler.resolve.insn;

import weac.compiler.precompile.Label;

public class IfNullJumpInsn extends JumpInsn {
    public IfNullJumpInsn(Label destination) {
        super(IF_NULL, destination);
    }
}
