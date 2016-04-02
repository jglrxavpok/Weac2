package weac.compiler.optimize;

import weac.compiler.CompilePhase;
import weac.compiler.precompile.Label;
import weac.compiler.resolve.insn.*;
import weac.compiler.resolve.structure.ResolvedClass;
import weac.compiler.resolve.structure.ResolvedField;
import weac.compiler.resolve.structure.ResolvedMethod;
import weac.compiler.resolve.structure.ResolvedSource;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class Optimizer extends CompilePhase<ResolvedSource, ResolvedSource> {
    @Override
    public ResolvedSource process(ResolvedSource resolvedSource) {
        resolvedSource.classes.forEach(this::optimizeClassMembers);
        return resolvedSource;
    }

    private void optimizeClassMembers(ResolvedClass resolvedClass) {
        resolvedClass.fields.forEach(this::optimizeField);
        resolvedClass.methods.forEach(m -> optimizeMethod(m, resolvedClass));
    }

    private void optimizeField(ResolvedField resolvedField) {
        optimize(resolvedField.defaultValue);
    }

    private void optimize(List<ResolvedInsn> insns) {

    }

    private void optimizeMethod(ResolvedMethod method, ResolvedClass owner) {
        boolean eligibleForTCO = isTCOptimizable(method, owner);
        if(eligibleForTCO)
            applyTCOptimization(method, owner);
        optimize(method.instructions);
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
                        method.instructions.set(index, new PopInsn());

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
     * Analyzes a single method and check if it is Tail-Call optimizable
     * @param method
     * @return
     */
    private boolean isTCOptimizable(ResolvedMethod method, ResolvedClass owner) {
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

    private boolean isCallToSelf(FunctionCallInsn call, ResolvedMethod method, ResolvedClass owner) {
        return call.getName().equals(method.name.getId()) && call.getOwner().getIdentifier().getId().equals(owner.fullName) && Arrays.equals(call.getArgTypes(), method.argumentTypes.toArray());
    }

    @Override
    public Class<ResolvedSource> getInputClass() {
        return ResolvedSource.class;
    }

    @Override
    public Class<ResolvedSource> getOutputClass() {
        return ResolvedSource.class;
    }
}
