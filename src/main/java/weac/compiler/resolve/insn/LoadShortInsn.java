package weac.compiler.resolve.insn;

public class LoadShortInsn extends ResolvedInsn {

    private final short number;

    public LoadShortInsn(short number) {
        super(LOAD_SHORT_CONSTANT);
        this.number = number;
    }

    public short getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
