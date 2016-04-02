package weac.compiler.resolve;

import weac.compiler.CompileUtils;
import weac.compiler.resolve.insn.*;
import weac.compiler.resolve.structure.ResolvedClass;
import weac.compiler.resolve.structure.ResolvedSource;
import weac.compiler.utils.WeacType;

import java.util.List;

public class MethodInfoSolver extends CompileUtils implements ResolveOpcodes {

    private int maxStack;
    private int stackCount;
    private int maxLocal;

    public void computeMaxs(List<ResolvedInsn> insns) {
        maxStack = 0;
        LocalVariableTableInsn tableInsn = ((LocalVariableTableInsn) insns.get(0));
        maxLocal = tableInsn.getLocals().size();
        for(int i = 0;i<insns.size();) {
            ResolvedInsn in = insns.get(i);
            switch (in.getOpcode()) {
                case LOAD_BOOL_CONSTANT:
                case LOAD_BYTE_CONSTANT:
                case LOAD_CHARACTER_CONSTANT:
                case LOAD_DOUBLE_CONSTANT:
                case LOAD_FLOAT_CONSTANT:
                case LOAD_INTEGER_CONSTANT:
                case LOAD_LOCAL_VARIABLE:
                case LOAD_LONG_CONSTANT:
                case LOAD_NULL:
                case LOAD_SHORT_CONSTANT:
                case LOAD_STRING_CONSTANT:
                    push(1);
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

                case IS_ZERO:
                case OBJ_EQUAL:
                case COMPARE_ZERO:
                    push(1);
                    break;

                default:
                    System.err.println("UNHANDLED: "+ResolveOpcodes.getName(in.getOpcode()));
                    break;

            }
            // incremented here in order to allow for easier jumps in the list
            i++;
        }
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
        stackCount -= count;
        maxStack = Math.max(stackCount, maxStack);
        if(stackCount < 0) {
            newError("Cannot pop operand off empty stack", -1); // todo line
        }
    }

    private void push(int count) {
        stackCount += count;
        maxStack = Math.max(stackCount, maxStack);
    }

    public int getMaxStack() {
        return maxStack;
    }

    public int getMaxLocal() {
        return maxLocal;
    }

    public void solveInfos(ResolvedSource resolvedSource) {
        resolvedSource.classes.stream().map(ResolvedClass::getMethods).forEach(list -> {
            list.forEach(m -> {
                computeMaxs(m.instructions);
                // TODO: Add stack and local maximums
            });
        });
    }
}
