package weac.compiler.resolve.insn;

import weac.compiler.precompile.Label;

public class GotoResInsn extends JumpInsn {

    public GotoResInsn(Label label) {
        super(JUMP, label);
    }

}
