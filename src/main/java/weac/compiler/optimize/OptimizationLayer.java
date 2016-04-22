package weac.compiler.optimize;

import weac.compiler.resolve.insn.ResolvedInsn;
import weac.compiler.resolve.structure.ResolvedClass;
import weac.compiler.resolve.structure.ResolvedField;
import weac.compiler.resolve.structure.ResolvedMethod;

import java.util.List;

public interface OptimizationLayer {

    /**
     * Returns a list with the optimization applied to the given instructions.<br/>
     * <b>Note:</b> whether the list is <code>instructions</code> with its content updated or an entirely new list is totally up to the layer.
     * @param method
     *          The method in which to optimize. If null, <code>field</code> must not be null
     * @param field
     *          The field in which to optimize. If null, <code>method</code> must not be null
     * @param owner
     *          The class in which to optimize
     * @param instructions
     *          The instructions to optimize
     * @return
     *          The list containing the optimized code.
     */
    List<ResolvedInsn> optimize(ResolvedMethod method, ResolvedField field, ResolvedClass owner, List<ResolvedInsn> instructions);
}
