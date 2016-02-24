package weac.compiler.resolve.insn;

public class WeacLoadStringInsn extends WeacResolvedInsn {

    private final String value;

    public WeacLoadStringInsn(String value) {
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
