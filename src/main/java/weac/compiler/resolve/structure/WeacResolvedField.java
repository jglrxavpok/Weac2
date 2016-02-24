package weac.compiler.resolve.structure;

import weac.compiler.resolve.insn.WeacResolvedInsn;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.WeacModifierType;
import weac.compiler.utils.WeacType;

import java.util.LinkedList;
import java.util.List;

public class WeacResolvedField {

    public WeacType type;
    public Identifier name;

    public final List<WeacResolvedInsn> defaultValue;

    public WeacModifierType access = WeacModifierType.PUBLIC;
    public boolean isCompilerSpecial;

    public WeacResolvedField() {
        defaultValue = new LinkedList<>();
    }
}
