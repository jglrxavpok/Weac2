package weac.compiler.resolve.insn;

public class LoadIntInsn extends ResolvedInsn {

    private final int number;

    public LoadIntInsn(int number) {
        super(LOAD_INTEGER_CONSTANT);
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
