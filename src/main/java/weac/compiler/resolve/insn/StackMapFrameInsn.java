package weac.compiler.resolve.insn;

import weac.compiler.resolve.structure.StackmapFrame;

public class StackMapFrameInsn extends ResolvedInsn {
    private final StackmapFrame frame;

    public StackMapFrameInsn(StackmapFrame frame) {
        super(STACK_MAP_FRAME);
        this.frame = frame;
    }

    public StackmapFrame getFrame() {
        return frame;
    }

    @Override
    public String toString() {
        return super.toString()+" "+frame;
    }
}
