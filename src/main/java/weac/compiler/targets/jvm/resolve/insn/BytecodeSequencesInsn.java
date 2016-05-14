package weac.compiler.targets.jvm.resolve.insn;

import weac.compiler.resolve.insn.NativeCodeInstruction;
import weac.compiler.resolve.insn.ResolvedInsn;
import weac.compiler.targets.jvm.resolve.BytecodeSequence;

import java.util.List;

public class BytecodeSequencesInsn extends ResolvedInsn implements NativeCodeInstruction {
    private final List<BytecodeSequence> sequences;

    public BytecodeSequencesInsn(List<BytecodeSequence> sequences) {
        super(NATIVE_CODE);
        this.sequences = sequences;
    }

    public List<BytecodeSequence> getSequences() {
        return sequences;
    }
}
