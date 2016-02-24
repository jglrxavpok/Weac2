package weac.compiler.resolve.insn;

public class LoadStringInsn extends ResolvedInsn {

    private final String value;

    public LoadStringInsn(String value) {
        super(LOAD_STRING_CONSTANT);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return super.toString()+" "+ value;
    }
}
