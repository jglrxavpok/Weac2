package weac.compiler.resolve.insn;

public class WeacLoadFloatInsn extends WeacResolvedInsn {

    private final float number;

    public WeacLoadFloatInsn(float number) {
        super(LOAD_BYTE_CONSTANT);
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
