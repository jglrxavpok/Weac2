package weac.compiler.precompile;

import weac.compiler.precompile.insn.PrecompiledInsn;

import java.util.LinkedList;
import java.util.List;

public class CodeBlock {

    private final Label start;
    private final Label end;
    private final List<List<PrecompiledInsn>> instructions;

    public CodeBlock() {
        start = new Label(-1);
        end = new Label(-1);
        instructions = new LinkedList<>();
    }

    public Label getStart() {
        return start;
    }

    public Label getEnd() {
        return end;
    }

    public List<List<PrecompiledInsn>> getInstructions() {
        return instructions;
    }
}
