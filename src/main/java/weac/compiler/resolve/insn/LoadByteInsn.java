package weac.compiler.resolve.insn;

public class LoadByteInsn extends ResolvedInsn {

    private final byte number;

    public LoadByteInsn(byte number) {
        super(LOAD_BYTE_CONSTANT);
        this.number = number;
    }

    public byte getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
