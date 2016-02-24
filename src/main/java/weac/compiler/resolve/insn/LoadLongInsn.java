package weac.compiler.resolve.insn;

public class LoadLongInsn extends ResolvedInsn {

    private final long number;

    public LoadLongInsn(long number) {
        super(LOAD_LONG_CONSTANT);
        this.number = number;
    }

    public long getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
