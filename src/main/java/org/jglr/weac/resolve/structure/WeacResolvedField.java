package org.jglr.weac.resolve.structure;

import org.jglr.weac.resolve.insn.WeacResolvedInsn;
import org.jglr.weac.utils.Identifier;
import org.jglr.weac.utils.WeacModifier;
import org.jglr.weac.utils.WeacModifierType;
import org.jglr.weac.utils.WeacType;

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
