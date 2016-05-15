package weac.compiler.precompile.insn;

public class PrecompiledLineNumber extends PrecompiledInsn {
    private final int lineNumber;

    public PrecompiledLineNumber(int lineNumber) {
        super(LINE_NUMBER);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
