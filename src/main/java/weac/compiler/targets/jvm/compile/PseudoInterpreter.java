package weac.compiler.targets.jvm.compile;

import weac.compiler.resolve.insn.*;

import java.util.List;
import java.util.Stack;

public class PseudoInterpreter {


    public Object interpret(List<ResolvedInsn> instructions) {
        Stack<Object> stack = new Stack<>();
        for(ResolvedInsn i : instructions) {
            if(i.getOpcode() == ResolveOpcodes.LOAD_BOOL_CONSTANT) {
                stack.push(((LoadBooleanInsn) i).getValue());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_BYTE_CONSTANT) {
                stack.push(((LoadByteInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_CHARACTER_CONSTANT) {
                stack.push(((LoadCharInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_DOUBLE_CONSTANT) {
                stack.push(((LoadDoubleInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_FLOAT_CONSTANT) {
                stack.push(((LoadFloatInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_INTEGER_CONSTANT) {
                stack.push(((LoadIntInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_LONG_CONSTANT) {
                stack.push(((LoadLongInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_SHORT_CONSTANT) {
                stack.push(((LoadShortInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_STRING_CONSTANT) {
                stack.push(((LoadStringInsn) i).getValue());
            }
        }
        if(stack.isEmpty()) {
            return null;
        }
        return stack.pop();
    }
}
