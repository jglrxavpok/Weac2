package weac.compiler.resolve.structure;

import weac.compiler.resolve.insn.ResolvedInsn;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.ModifierType;
import weac.compiler.utils.WeacType;

import java.util.LinkedList;
import java.util.List;

public class ResolvedField {

    public WeacType type;
    public Identifier name;

    public final List<ResolvedInsn> defaultValue;

    public ModifierType access = ModifierType.PUBLIC;
    public boolean isCompilerSpecial;

    public ResolvedField() {
        defaultValue = new LinkedList<>();
    }
}
