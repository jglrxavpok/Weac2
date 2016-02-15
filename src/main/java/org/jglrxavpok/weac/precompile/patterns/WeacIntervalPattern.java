package org.jglrxavpok.weac.precompile.patterns;

import org.jglrxavpok.weac.patterns.WeacInstructionPattern;
import org.jglrxavpok.weac.precompile.insn.PrecompileOpcodes;
import org.jglrxavpok.weac.precompile.insn.WeacOperatorInsn;
import org.jglrxavpok.weac.precompile.insn.WeacPrecompiledInsn;
import org.jglrxavpok.weac.utils.EnumOperators;

import java.util.List;

public class WeacIntervalPattern extends WeacInstructionPattern<WeacPrecompiledInsn> {

    @Override
    public int[] getOpcodes() {
        return new int[] {
            PrecompileOpcodes.BINARY_OPERATOR, PrecompileOpcodes.CREATE_ARRAY
        };
    }

    @Override
    protected boolean isValid(WeacPrecompiledInsn insn, Integer expectedCode, int index) {
        if(super.isValid(insn, expectedCode, index)) {
            if(expectedCode == PrecompileOpcodes.BINARY_OPERATOR) {
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
