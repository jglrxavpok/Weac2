package weac.compiler.resolve.insn;

public class LoadBooleanInsn extends ResolvedInsn {

    private final boolean number;

    public LoadBooleanInsn(boolean number) {
        super(LOAD_BOOL_CONSTANT);
        this.number = number;
    }

    public boolean getValue() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
