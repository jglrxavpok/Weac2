package weac.compiler.optimize;

import weac.compiler.resolve.insn.*;
import weac.compiler.resolve.structure.ResolvedClass;
import weac.compiler.resolve.structure.ResolvedField;
import weac.compiler.resolve.structure.ResolvedMethod;

import java.util.ArrayList;
import java.util.List;

public class NeutralElementsLayer implements OptimizationLayer {
    @Override
    public List<ResolvedInsn> optimize(ResolvedMethod method, ResolvedField field, ResolvedClass owner, List<ResolvedInsn> instructions) {
        List<ResolvedInsn> result = new ArrayList<>();
        for (int i = 0; i < instructions.size(); i++) {
            ResolvedInsn insn = instructions.get(i);
            if(i > 0) {
                int toRemoveCount = 1;
                ResolvedInsn previous = instructions.get(i-1);
                if(previous instanceof CastInsn && i > 1) {
                    previous = instructions.get(i-2);
                    toRemoveCount++;
                }
                // TODO: Support 0+foo and 1*bar instead of just foo-0 and bar/1?
                if(insn instanceof SubtractInsn) {
                    if(isLoading(previous, 0)) {
                        for (int j = 0; j < toRemoveCount; j++) {
                            result.remove(result.size()-1);
                        }
                        continue;
                    }
                } else if(insn instanceof DivideInsn) {
                    if(isLoading(previous, 1)) {
                        for (int j = 0; j < toRemoveCount; j++) {
                            result.remove(result.size()-1);
                        }
                        continue;
                    }
                }
            }
            result.add(insn);
        }
        return result;
    }

    private boolean isLoading(ResolvedInsn insn, double value) {
        if(insn instanceof LoadByteInsn)
            return ((LoadByteInsn) insn).getNumber() == value;
        if(insn instanceof LoadCharInsn)
            return ((LoadCharInsn) insn).getNumber() == value;
        if(insn instanceof LoadDoubleInsn)
            return ((LoadDoubleInsn) insn).getNumber() == value;
        if(insn instanceof LoadFloatInsn)
            return ((LoadFloatInsn) insn).getNumber() == value;
        if(insn instanceof LoadIntInsn)
            return ((LoadIntInsn) insn).getNumber() == value;
        if(insn instanceof LoadLongInsn)
            return ((LoadLongInsn) insn).getNumber() == value;
        if(insn instanceof LoadShortInsn)
            return ((LoadShortInsn) insn).getNumber() == value;
        return false;
    }
}
