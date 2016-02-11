package org.jglrxavpok.weac.compile;

import org.jglrxavpok.weac.resolve.insn.*;

import java.util.List;
import java.util.Stack;

public class WeacPseudoInterpreter {


    public Object interpret(List<WeacResolvedInsn> instructions) {
        Stack<Object> stack = new Stack<>();
        for(WeacResolvedInsn i : instructions) {
            if(i.getOpcode() == ResolveOpcodes.LOAD_BOOL_CONSTANT) {
                stack.push(((WeacLoadBooleanInsn) i).getValue());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_BYTE_CONSTANT) {
                stack.push(((WeacLoadByteInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_CHARACTER_CONSTANT) {
                stack.push(((WeacLoadCharInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_DOUBLE_CONSTANT) {
                stack.push(((WeacLoadDoubleInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_FLOAT_CONSTANT) {
                stack.push(((WeacLoadFloatInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_INTEGER_CONSTANT) {
                stack.push(((WeacLoadIntInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_LONG_CONSTANT) {
                stack.push(((WeacLoadLongInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_SHORT_CONSTANT) {
                stack.push(((WeacLoadShortInsn) i).getNumber());
            } else if(i.getOpcode() == ResolveOpcodes.LOAD_STRING_CONSTANT) {
                stack.push(((WeacLoadStringInsn) i).getValue());
            }
        }
        if(stack.isEmpty()) {
            return null;
        }
        return stack.pop();
    }
}
