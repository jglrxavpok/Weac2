package weac.compiler.optimize;

import weac.compiler.precompile.Label;
import weac.compiler.resolve.insn.*;
import weac.compiler.resolve.structure.ResolvedClass;
import weac.compiler.resolve.structure.ResolvedField;
import weac.compiler.resolve.structure.ResolvedMethod;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class TailCallLayer implements OptimizationLayer {
    @Override
    public List<ResolvedInsn> optimize(ResolvedMethod method, ResolvedField field, ResolvedClass owner, List<ResolvedInsn> instructions) {
        boolean eligibleForTCO = isTCOptimizable(method, owner);
        if(eligibleForTCO)
            applyTCOptimization(method, owner);
        return instructions;
    }

    /**
     * Optimizes the method by handling tail call recursion
     * @param method
     */
    private void applyTCOptimization(ResolvedMethod method, ResolvedClass owner) {
        Label startLabel = new Label(-50);
        // 1 because the 0th instruction is the local variable table
        method.instructions.add(1, new ResolvedLabelInsn(startLabel));
        for (int i = 0; i < method.instructions.size(); i++) {
            ResolvedInsn prev = null;
            int prevIndex = i-1;
            if(i > 0) {
                prev = method.instructions.get(prevIndex);
            }
            ResolvedInsn in = method.instructions.get(i);
            if (prev != null && in.getOpcode() >= ResolveOpcodes.FIRST_RETURN_OPCODE && in.getOpcode() <= ResolveOpcodes.LAST_RETURN_OPCODE) {
                if(prev instanceof FunctionCallInsn) {
                    FunctionCallInsn call = ((FunctionCallInsn) prev);
                    if(isCallToSelf(call, method, owner)) {

                        // replace corresponding function start instruction by pop instruction
                        int index = findCorrespondingFuncStart(method.instructions, call, i);
                        method.instructions.set(index, new PopInsn(owner.name));

                        method.instructions.remove(i);
                        i--;
                        for(int j = 0;j<call.getArgCount();j++) {
                            method.instructions.add(i, new StoreVarInsn(j+1, call.getArgTypes()[j]));
                        }
                        method.instructions.set(i+call.getArgCount(), new GotoResInsn(startLabel));
                    }
                }
            }
        }
    }

    private int findCorrespondingFuncStart(List<ResolvedInsn> instructions, FunctionCallInsn call, int index) {
        for(;index >= 0;index--) {
            ResolvedInsn in = instructions.get(index);
            if(in instanceof FunctionStartResInsn) {
                FunctionStartResInsn startInsn = ((FunctionStartResInsn) in);
                if(call.getName().equals(startInsn.getFunctionName()) && call.getArgCount() == startInsn.getArgCount() && call.getOwner().equals(startInsn.getOwner())) {
                    return index;
                }
            }
        }
        return -1;
    }

    /**
     * Analyzes a single method and check if it is Tail-Call optimisable
     * @param method
     * @return
     */
    private boolean isTCOptimizable(ResolvedMethod method, ResolvedClass owner) {
        if(method == null)
            return false;
        ListIterator<ResolvedInsn> iterator = method.instructions.listIterator();
        while (iterator.hasNext()) {
            ResolvedInsn prev = null;
            if(iterator.hasPrevious()) {
                prev = method.instructions.get(iterator.previousIndex());
            }
            ResolvedInsn in = iterator.next();
            if (prev != null && in.getOpcode() >= ResolveOpcodes.FIRST_RETURN_OPCODE && in.getOpcode() <= ResolveOpcodes.LAST_RETURN_OPCODE) {
                if(prev instanceof FunctionCallInsn) {
                    FunctionCallInsn call = ((FunctionCallInsn) prev);
                    if(isCallToSelf(call, method, owner)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if a function call is call to <u>exactly the same</u> function that the call is located in.
     * @param call
     *          The function call instruction
     * @param method
     *          The method from which it is called
     * @param owner
     *          The class owner of the method
     * @return
     *          <code>true</code> if the function is calling itself, <code>false</code> otherwise
     */
    private boolean isCallToSelf(FunctionCallInsn call, ResolvedMethod method, ResolvedClass owner) {
        return call.getName().equals(method.name.getId()) && call.getOwner().getIdentifier().getId().equals(owner.fullName) && Arrays.equals(call.getArgTypes(), method.argumentTypes.toArray());
    }
}
