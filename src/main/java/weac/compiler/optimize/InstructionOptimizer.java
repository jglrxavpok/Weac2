package weac.compiler.optimize;

import weac.compiler.resolve.insn.ResolvedInsn;

import java.util.List;

public interface InstructionOptimizer {

    /**
     * Returns a <b>new</b> list with the optimization applied to the given instructions
     * @param instructions
     *          The instructions to optimize
     * @return
     *          A new list with the optimized code
     */
    List<ResolvedInsn> optimize(List<ResolvedInsn> instructions);
}
