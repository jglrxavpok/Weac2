package org.jglr.weac.precompile;

import org.jglr.weac.precompile.insn.WeacPrecompiledInsn;

import java.util.LinkedList;
import java.util.List;

public class WeacCodeBlock {

    private final WeacLabel start;
    private final WeacLabel end;
    private final List<List<WeacPrecompiledInsn>> instructions;

    public WeacCodeBlock() {
        start = new WeacLabel(0);
        end = new WeacLabel(-1);
        instructions = new LinkedList<>();
    }

    public WeacLabel getStart() {
        return start;
    }

    public WeacLabel getEnd() {
        return end;
    }

    public List<List<WeacPrecompiledInsn>> getInstructions() {
        return instructions;
    }
}
