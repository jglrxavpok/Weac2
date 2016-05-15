package weac.compiler.resolve.insn;

public class LineNumberInstruction extends ResolvedInsn {
    private final int lineNumber;

    public LineNumberInstruction(int lineNumber) {
        super(LINE_NUMBER);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
