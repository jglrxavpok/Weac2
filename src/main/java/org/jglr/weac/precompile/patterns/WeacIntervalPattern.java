package org.jglr.weac.precompile.patterns;

import org.jglr.weac.precompile.insn.PrecompileOpcodes;
import org.jglr.weac.precompile.insn.WeacOperatorInsn;
import org.jglr.weac.precompile.insn.WeacPrecompiledInsn;
import org.jglr.weac.utils.EnumOperators;

import java.util.List;

public class WeacIntervalPattern extends WeacInstructionPattern {

    @Override
    public int[] getOpcodes() {
        return new int[] {
            BINARY_OPERATOR, CREATE_ARRAY
        };
    }

    @Override
    protected boolean isValid(WeacPrecompiledInsn insn, int expectedCode, int index) {
        if(super.isValid(insn, expectedCode, index)) {
            if(expectedCode == BINARY_OPERATOR) {
                WeacOperatorInsn op = ((WeacOperatorInsn) insn);
                return op.getOperator() == EnumOperators.INTERVAL_SEPARATOR;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public int consumeCount(List<WeacPrecompiledInsn> insns, int index) {
        int start = index;
        index += 2;
        for(;index < insns.size();index++) {
            if(insns.get(index).getOpcode() != PrecompileOpcodes.STORE_ARRAY)
                break;
        }
        return index-start;
    }

    @Override
    public void output(List<WeacPrecompiledInsn> original, int i, List<WeacPrecompiledInsn> output) {
        output.add(original.get(i));
    }
}
