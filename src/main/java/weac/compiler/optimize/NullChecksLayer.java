package weac.compiler.optimize;

import weac.compiler.resolve.insn.*;
import weac.compiler.resolve.structure.ResolvedClass;
import weac.compiler.resolve.structure.ResolvedField;
import weac.compiler.resolve.structure.ResolvedMethod;

import java.util.ArrayList;
import java.util.List;

public class NullChecksLayer implements OptimizationLayer {
    @Override
    public List<ResolvedInsn> optimize(ResolvedMethod method, ResolvedField field, ResolvedClass owner, List<ResolvedInsn> instructions) {
        List<ResolvedInsn> result = new ArrayList<>();
        for (int i = 0; i < instructions.size(); i++) {
            ResolvedInsn insn = instructions.get(i);
            if(insn instanceof LoadNullInsn) {
                if(i < instructions.size()-3) {
                    ResolvedInsn next = instructions.get(i+1);
                    ResolvedInsn next1 = instructions.get(i+2);
                    if(next instanceof ObjectEqualInsn && next1 instanceof IfNotJumpResInsn) {
                        result.add(new IfNotNullJumpInsn(((IfNotJumpResInsn) next1).getDestination()));
                        i += 2;
                        continue;
                    }
                }
            }
            result.add(insn);
        }
        return result;
    }
}
