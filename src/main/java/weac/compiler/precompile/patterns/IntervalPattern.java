package weac.compiler.precompile.patterns;

import weac.compiler.patterns.InstructionPattern;
import weac.compiler.precompile.insn.PrecompileOpcodes;
import weac.compiler.precompile.insn.PrecompiledInsn;
import weac.compiler.precompile.insn.OperatorInsn;
import weac.compiler.utils.EnumOperators;

import java.util.List;

public class IntervalPattern extends InstructionPattern<PrecompiledInsn> {

    @Override
    public int[] getOpcodes() {
        return new int[] {
            PrecompileOpcodes.BINARY_OPERATOR, PrecompileOpcodes.CREATE_ARRAY
        };
    }

    @Override
    protected boolean isValid(PrecompiledInsn insn, Integer expectedCode, int index) {
        if(super.isValid(insn, expectedCode, index)) {
            if(expectedCode == PrecompileOpcodes.BINARY_OPERATOR) {
                OperatorInsn op = ((OperatorInsn) insn);
                return op.getOperator() == EnumOperators.INTERVAL_SEPARATOR;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public int consumeCount(List<PrecompiledInsn> insns, int index) {
        int start = index;
        index += 2;
        for(;index < insns.size();index++) {
            if(insns.get(index).getOpcode() != PrecompileOpcodes.STORE_ARRAY)
                break;
        }
        index++;
        return index-start;
    }

    @Override
    public void output(List<PrecompiledInsn> original, int i, List<PrecompiledInsn> output) {
        output.add(original.get(i));
    }
}
