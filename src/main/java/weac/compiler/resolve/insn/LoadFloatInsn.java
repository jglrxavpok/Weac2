package weac.compiler.resolve.insn;

public class LoadFloatInsn extends ResolvedInsn {

    private final float number;

    public LoadFloatInsn(float number) {
        super(LOAD_FLOAT_CONSTANT);
        this.number = number;
    }

    public float getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
