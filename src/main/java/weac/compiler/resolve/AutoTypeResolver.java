package weac.compiler.resolve;

import weac.compiler.resolve.insn.*;
import weac.compiler.utils.WeacType;

import java.util.List;
import java.util.Stack;

public class AutoTypeResolver implements ResolveOpcodes {

    private final Stack<WeacType> typeStack;

    public AutoTypeResolver() {
        typeStack = new Stack<>();
    }

    public WeacType findEndType(List<ResolvedInsn> insns) {
        for(ResolvedInsn in : insns) {
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
                    CastInsn castInsn = (CastInsn) in;
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
                    if (!callInsn.isStatic()) {
                        popType(1);
                    }
                    popType(callInsn.getArgCount());
                    if (!callInsn.getReturnType().equals(WeacType.VOID_TYPE)) {
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
                    LoadFieldInsn loadFieldInsn = (LoadFieldInsn) in;
                    if (!loadFieldInsn.isStatic()) {
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
                    StoreFieldInsn storeFieldInsn = (StoreFieldInsn) in;
                    if (!storeFieldInsn.isStatic()) {
                        popType(1);
                    }
                    popType(1);
                    break;

                default:
                    System.err.println("UNRESOLVED IN AUTO RESOLVER: "+in.getOpcode()+" "+in.toString());
                    break;
            }
        }
        return typeStack.isEmpty() ? WeacType.VOID_TYPE : typeStack.pop();
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

}
