package weac.compiler.resolve;

import weac.compiler.CompileUtils;
import weac.compiler.precompile.Label;
import weac.compiler.resolve.insn.*;
import weac.compiler.resolve.structure.ResolvedMethod;
import weac.compiler.resolve.structure.ResolvedSource;
import weac.compiler.resolve.structure.StackmapFrame;
import weac.compiler.utils.WeacType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MethodInfoSolver extends CompileUtils implements ResolveOpcodes {

    private final Map<Integer, Integer> stackSizes;
    private int maxStack;
    private int stackSize;
    private int maxLocal;

    private Stack<WeacType> typeStack;

    public MethodInfoSolver() {
        stackSizes = new HashMap<>();
        typeStack = new Stack<>();
    }

    public void computeMaxs(List<ResolvedInsn> insns) {
        stackSizes.clear();
        stackSize = 0;
        maxStack = 0;
        LocalVariableTableInsn tableInsn = ((LocalVariableTableInsn) insns.get(0));
        maxLocal = tableInsn.getLocals().size();
        for(int i = 0;i<insns.size();) {
            ResolvedInsn in = insns.get(i);
            if (!stackSizes.containsKey(i)) {
                stackSizes.put(i, stackSize);
            } else if (stackSizes.get(i) != stackSize) {
                    newError("There was already a stack size for this instruction and it does not match with the current one! " + stackSize + " (" + in.toString() + ")", -1); // todo line
            } else { // we might have gone through all the possibilities, check if so
                boolean allDone = true;
                for(int j = 0;j<insns.size();j++) {
                    ResolvedInsn in0 = insns.get(j);
                    if(!stackSizes.containsKey(j) && in0.getOpcode() != ResolveOpcodes.RETURN && in0.getOpcode() != ResolveOpcodes.LABEL) {
                        allDone = false;
                        break;
                    }
                }
                if(allDone) { // we're done
                    return;
                }
            }
            switch (in.getOpcode()) {
                case LOAD_BOOL_CONSTANT:
                case LOAD_BYTE_CONSTANT:
                case LOAD_CHARACTER_CONSTANT:
                case LOAD_DOUBLE_CONSTANT:
                case LOAD_FLOAT_CONSTANT:
                case LOAD_INTEGER_CONSTANT:
                case LOAD_LONG_CONSTANT:
                case LOAD_NULL:
                case LOAD_SHORT_CONSTANT:
                case LOAD_STRING_CONSTANT:
                    push(1);
                    break;

                case LOAD_LOCAL_VARIABLE:
                    push(1);
                    maxLocal = Math.max(maxLocal, ((LoadVariableInsn)in).getVarIndex());
                    break;

                case INT_RETURN:
                case OBJ_RETURN:
                case FLOAT_RETURN:
                case BOOL_RETURN:
                case BYTE_RETURN:
                case DOUBLE_RETURN:
                case CHAR_RETURN:
                case SHORT_RETURN:
                case LONG_RETURN:

                case POP:
                case THROW:
                    pop(1);
                    break;

                // does not change the stack
                case FUNCTION_START:
                case LABEL:
                case RETURN:
                case NULL:
                case CAST:
                case LOCAL_VARIABLE_TABLE:
                    break;

                case ADD:
                case DIVIDE:
                case MULTIPLY:
                case SUBTRACT:
                case MODULUS:
                    pop(2);
                    push(1);
                    break;

                case DUP:
                    pop(1);
                    push(2);
                    break;

                case FUNCTION_CALL:
                    handleFunctionCall(((FunctionCallInsn) in));
                    break;

                case LOAD_FIELD:
                    LoadFieldInsn loadFieldInsn = ((LoadFieldInsn) in);
                    if(!loadFieldInsn.isStatic()) {
                        pop(1);
                    }
                    push(1);
                    break;

                case STORE_FIELD:
                    StoreFieldInsn storeFieldInsn = ((StoreFieldInsn) in);
                    if(!storeFieldInsn.isStatic()) {
                        pop(1);
                    }
                    pop(1);
                    break;

                case STORE_LOCAL_VARIABLE:
                    pop(1);
                    maxLocal = Math.max(maxLocal, ((StoreVarInsn)in).getLocalIndex());
                    break;

                case OBJ_EQUAL:
                    pop(2);
                    push(1);
                    break;

                case COMPARE_ZERO:
                    pop(2);
                    push(1);
                    break;

                case IS_ZERO:
                    pop(1);
                    push(1);
                    break;

                case LESS:
                case LESS_OR_EQUAL:
                case GREATER:
                case GREATER_OR_EQUAL:
                    pop(1);
                    push(1);
                    break;

                case NEW:
                    push(1);
                    break;

                case IF_NOT_TRUE_JUMP:
                    pop(1);
                    handleBranching(((JumpInsn) in), insns);
                    break;

                case JUMP:
                    i = handleDirectJump(((JumpInsn) in), insns);
                    continue;

                default:
                    System.err.println("UNHANDLED: "+ResolveOpcodes.getName(in.getOpcode()));
                    break;

            }
            // incremented here in order to allow for easier jumps in the list
            i++;
        }
        maxLocal++;
    }

    private void handleBranching(JumpInsn in, List<ResolvedInsn> insns) {

    }

    private int handleDirectJump(JumpInsn in, List<ResolvedInsn> insns) {
        int currentIndex = insns.indexOf(in);
        Label to = in.getDestination();
        int index = findCorrespondingIndex(to, insns);
        if(currentIndex > index) { // backwards jump
            int size = stackSizes.get(index);
            if(size != stackSize) {
                newError("Stack sizes do not match after jump to "+to, -1); // todo line
            }
        } else if(currentIndex < index) { // forward jump
            stackSizes.put(index, stackSize);
        }
        return index;
    }

    private int findCorrespondingIndex(Label label, List<ResolvedInsn> insns) {
        for (int i = 0; i < insns.size(); i++) {
            ResolvedInsn in = insns.get(i);
            if(in.getOpcode() == ResolveOpcodes.LABEL) {
                if(((ResolvedLabelInsn) in).getLabel().equals(label)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void handleFunctionCall(FunctionCallInsn in) {
        if(!in.isStatic()) {
            pop(1);
        }
        pop(in.getArgCount());
        if(!in.getReturnType().equals(WeacType.VOID_TYPE))
            push(1);
    }

    private void pop(int count) {
        stackSize -= count;
        maxStack = Math.max(stackSize, maxStack);
        if(stackSize < 0) {
            newError("Cannot pop operand off empty stack", -1); // todo line
        }
    }

    private void push(int count) {
        stackSize += count;
        maxStack = Math.max(stackSize, maxStack);
    }

    public void computeFrames(List<ResolvedInsn> insns, StackmapFrame initialFrame) {
        typeStack.clear();
        insns.add(0, new StackMapFrameInsn(initialFrame)); // TEST
        for(int i = 0;i<insns.size();i++) {
            ResolvedInsn in = insns.get(i);
            if(in instanceof ResolvedLabelInsn) {
                if(isJumpTarget(((ResolvedLabelInsn) in).getLabel(), insns)) {
                    insns.add(i+1, new StackMapFrameInsn(createFrame(initialFrame)));
                }
            } else {
                switch (in.getOpcode()) {
                    case SUBTRACT:
                    case MULTIPLY:
                    case DIVIDE:
                    case MODULUS:
                    case ADD: {
                        OperationInsn operationInsn = ((OperationInsn) in);
                        WeacType result = operationInsn.getResultType();
                        popType(2);
                        pushType(result);
                    }
                    break;

                    case BOOL_RETURN:
                    case INT_RETURN:
                    case CHAR_RETURN:
                    case DOUBLE_RETURN:
                    case BYTE_RETURN:
                    case FLOAT_RETURN:
                    case LONG_RETURN:
                    case SHORT_RETURN:
                    case OBJ_RETURN:
                        popType(1);
                        break;

                    case CAST: {
                        CastInsn castInsn = (CastInsn)in;
                        popType(1);
                        pushType(castInsn.getTo());
                    }
                    break;

                    case COMPARE_ZERO: {
                        popType(2);
                        pushType(WeacType.BOOLEAN_TYPE);
                    }
                    break;

                    case DUP: {
                        WeacType t = popType(1);
                        pushType(t);
                        pushType(t);
                    }
                    break;

                    case FUNCTION_CALL: {
                        FunctionCallInsn callInsn = ((FunctionCallInsn) in);
                        if(!callInsn.isStatic()) {
                            popType(1);
                        }
                        popType(callInsn.getArgCount());
                        if(!callInsn.getReturnType().equals(WeacType.VOID_TYPE)) {
                            pushType(callInsn.getReturnType());
                        }
                    }
                    break;

                    case GREATER:
                    case GREATER_OR_EQUAL:
                    case LESS:
                    case LESS_OR_EQUAL:
                        popType(1);
                        pushType(WeacType.BOOLEAN_TYPE);
                        break;

                    case IF_NOT_TRUE_JUMP:
                        popType(1);
                        break;

                    case JUMP:
                        break;

                    case IS_ZERO:
                        popType(1);
                        pushType(WeacType.BOOLEAN_TYPE);
                        break;

                    case LOAD_BOOL_CONSTANT:
                        pushType(WeacType.BOOLEAN_TYPE);
                        break;

                    case LOAD_BYTE_CONSTANT:
                        pushType(WeacType.BYTE_TYPE);
                        break;

                    case LOAD_CHARACTER_CONSTANT:
                        pushType(WeacType.CHAR_TYPE);
                        break;

                    case LOAD_DOUBLE_CONSTANT:
                        pushType(WeacType.DOUBLE_TYPE);
                        break;

                    case LOAD_FLOAT_CONSTANT:
                        pushType(WeacType.FLOAT_TYPE);
                        break;

                    case LOAD_INTEGER_CONSTANT:
                        pushType(WeacType.INTEGER_TYPE);
                        break;

                    case LOAD_LONG_CONSTANT:
                        pushType(WeacType.LONG_TYPE);
                        break;

                    case LOAD_SHORT_CONSTANT:
                        pushType(WeacType.SHORT_TYPE);
                        break;

                    case LOAD_NULL:
                        pushType(WeacType.JOBJECT_TYPE);
                        break;

                    case LOAD_STRING_CONSTANT:
                        pushType(WeacType.STRING_TYPE);
                        break;

                    case LOAD_LOCAL_VARIABLE:
                        pushType(((LoadVariableInsn) in).getVarType());
                        break;

                    case STORE_LOCAL_VARIABLE:
                        popType(1);
                        break;

                    case NEW:
                        pushType(((NewInsn) in).getType());
                        break;

                    case POP:
                        popType(1);
                        break;

                    case RETURN:
                        popType(typeStack.size());
                        break;

                    case THROW:
                        popType(typeStack.size());
                        pushType(new WeacType(null, "java.lang.Throwable", true));
                        break;

                    case LOAD_FIELD:
                        LoadFieldInsn loadFieldInsn = (LoadFieldInsn)in;
                        if(!loadFieldInsn.isStatic()) {
                            popType(1);
                        }
                        pushType(loadFieldInsn.getType());
                        break;

                    case FUNCTION_START:
                    case STACK_MAP_FRAME:
                    case LOCAL_VARIABLE_TABLE:
                    case MAXS:
                        break;

                    case STORE_FIELD:
                        StoreFieldInsn storeFieldInsn = (StoreFieldInsn)in;
                        if(!storeFieldInsn.isStatic()) {
                            popType(1);
                        }
                        popType(1);
                        break;

                    default:
                        System.out.println("Unhandled in frames: "+ResolveOpcodes.getName(in.getOpcode()));
                        break;
                }
            }
        }
    }

    private StackmapFrame compressFrame(StackmapFrame frame, StackmapFrame reference) {

        return null;
    }

    private StackmapFrame createFrame(StackmapFrame initial) {
        return initial.append(typeStack).setStackSize(typeStack.size());
    }

    private void pushType(WeacType type) {
        typeStack.push(type);
    }

    private WeacType popType(int count) {
        WeacType last = null;
        for (int i = 0; i < count; i++) {
            last = typeStack.pop();
        }
        return last;
    }

    private boolean isJumpTarget(Label label, List<ResolvedInsn> insns) {
        for(ResolvedInsn in : insns) {
            if(in instanceof JumpInsn) {
                if(label.equals(((JumpInsn) in).getDestination())) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public int getMaxLocal() {
        return maxLocal;
    }

    public void solveInfos(ResolvedSource resolvedSource) {
        resolvedSource.classes.stream().forEach(owner -> {
            List<ResolvedMethod> methodList = owner.getMethods();
            methodList.forEach(m -> {
                computeMaxs(m.instructions);
                m.instructions.add(new MaxsInsn(maxStack, maxLocal));

                // create initial frame
                StackmapFrame initialFrame = new StackmapFrame(0).append(owner.fullName).append(m.argumentTypes);
                computeFrames(m.instructions, initialFrame);
            });
        });
    }
}
