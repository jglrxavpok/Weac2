package org.jglrxavpok.weac.resolve.structure;

import org.jglrxavpok.weac.resolve.insn.WeacResolvedInsn;
import org.jglrxavpok.weac.utils.Identifier;
import org.jglrxavpok.weac.utils.WeacModifierType;
import org.jglrxavpok.weac.utils.WeacType;

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
