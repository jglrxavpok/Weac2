package weac.compiler.resolve.insn;

import weac.compiler.precompile.Label;

public class IfNotJumpResInsn extends JumpInsn {

    public IfNotJumpResInsn(Label jumpTo) {
        super(IF_NOT_TRUE_JUMP, jumpTo);
    }

}
