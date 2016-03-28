package weac.compiler.resolve.insn;

import weac.compiler.resolve.VariableMap;
import weac.compiler.resolve.values.Value;
import weac.compiler.resolve.values.VariableValue;
import weac.compiler.utils.WeacType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class LocalVariableTableInsn extends ResolvedInsn {
    private final List<VariableValue> locals;

    public LocalVariableTableInsn(VariableMap varMap) {
        super(LOCAL_VARIABLE_TABLE);
        int count = varMap.getCurrentLocalIndex();
        locals = new LinkedList<>();
        for(int i = 0;i<count;i++) {
            String name = varMap.getLocalName(i);
            WeacType type = varMap.getLocalType(name);
            locals.add(new VariableValue(name, type, i));
        }
    }

    public List<VariableValue> getLocals() {
        return locals;
    }

}
