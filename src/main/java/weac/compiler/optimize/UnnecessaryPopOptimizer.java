package weac.compiler.optimize;

import weac.compiler.resolve.insn.PopInsn;
import weac.compiler.resolve.insn.ResolveOpcodes;
import weac.compiler.resolve.insn.ResolvedInsn;

import java.util.ArrayList;
import java.util.List;

public class UnnecessaryPopOptimizer implements InstructionOptimizer, ResolveOpcodes {
    @Override
    public List<ResolvedInsn> optimize(List<ResolvedInsn> instructions) {
        List<ResolvedInsn> optimized = new ArrayList<>();
        for (int i = 0; i < instructions.size(); i++) {
            ResolvedInsn insn = instructions.get(i);
            ResolvedInsn next = null;
            if(i < instructions.size()-1)
                next = instructions.get(i+1);
            if(next instanceof PopInsn) {
                int opcode = insn.getOpcode();
                switch (opcode) {
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

                    case LOAD_FIELD:
                    case LOAD_LOCAL_VARIABLE:
                    case DUP:
                        i++; // skip next opcode
                        continue;
                }
            }
            optimized.add(insn);
        }
        return optimized;
    }
}
