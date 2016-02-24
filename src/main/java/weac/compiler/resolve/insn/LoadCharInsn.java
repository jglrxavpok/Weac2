package weac.compiler.resolve.insn;

public class LoadCharInsn extends ResolvedInsn {

    private final char number;

    public LoadCharInsn(char number) {
        super(LOAD_CHARACTER_CONSTANT);
        this.number = number;
    }

    public char getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
