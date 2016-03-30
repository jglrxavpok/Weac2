package weac.compiler.resolve.insn;

public class LoadDoubleInsn extends ResolvedInsn {

    private final double number;

    public LoadDoubleInsn(double number) {
        super(LOAD_DOUBLE_CONSTANT);
        this.number = number;
    }

    public double getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
