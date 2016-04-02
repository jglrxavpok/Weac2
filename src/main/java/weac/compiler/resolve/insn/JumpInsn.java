package weac.compiler.resolve.insn;

import weac.compiler.precompile.Label;

public class JumpInsn extends ResolvedInsn {

    private final Label destination;

    public JumpInsn(int opcode, Label destination) {
        super(opcode);
        this.destination = destination;
    }

    public Label getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return super.toString()+" "+ destination;
    }

}
