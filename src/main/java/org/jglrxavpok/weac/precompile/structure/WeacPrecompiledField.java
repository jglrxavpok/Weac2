package org.jglrxavpok.weac.precompile.structure;

import org.jglrxavpok.weac.precompile.insn.WeacPrecompiledInsn;
import org.jglrxavpok.weac.utils.Identifier;
import org.jglrxavpok.weac.utils.WeacModifierType;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a field that has be extracted from the source code.
 */
public class WeacPrecompiledField {

    /**
     * The type of the field
     */
    public Identifier type;

    /**
     * The name of the field
     */
    public Identifier name;

    /**
     * The default value of this field. Not yet resolved
     */
    public final List<WeacPrecompiledInsn> defaultValue;

    /**
     * The access modifier of this field
     */
    public WeacModifierType access = WeacModifierType.PUBLIC;

    public final List<WeacPrecompiledAnnotation> annotations;

    public boolean isCompilerSpecial;

    public WeacPrecompiledField() {
        annotations = new LinkedList<>();
        defaultValue = new LinkedList<>();
    }

}
