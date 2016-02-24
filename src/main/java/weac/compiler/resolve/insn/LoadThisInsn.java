package weac.compiler.resolve.insn;

public class LoadThisInsn extends LoadVariableInsn {
    public LoadThisInsn() {
        super(0);
    }

    @Override
    public String toString() {
        return super.toString()+" this";
    }
}
