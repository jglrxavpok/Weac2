package weac.compiler.resolve.insn;

public class MaxsInsn extends ResolvedInsn {
    private final int maxStack;
    private final int maxLocal;

    public MaxsInsn(int maxStack, int maxLocal) {
        super(MAXS);
        this.maxStack = maxStack;
        this.maxLocal = maxLocal;
    }

    public int getMaxLocal() {
        return maxLocal;
    }

    public int getMaxStack() {
        return maxStack;
    }

    @Override
    public String toString() {
        return super.toString()+" local: "+maxLocal+", stack: "+maxStack;
    }
}
