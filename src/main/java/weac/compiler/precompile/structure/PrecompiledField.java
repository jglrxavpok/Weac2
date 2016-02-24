package weac.compiler.precompile.structure;

import weac.compiler.precompile.insn.PrecompiledInsn;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.ModifierType;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a field that has be extracted from the source code.
 */
public class PrecompiledField {

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
    public final List<PrecompiledInsn> defaultValue;

    /**
     * The access modifier of this field
     */
    public ModifierType access = ModifierType.PUBLIC;

    public final List<PrecompiledAnnotation> annotations;

    public boolean isCompilerSpecial;

    public PrecompiledField() {
        annotations = new LinkedList<>();
        defaultValue = new LinkedList<>();
    }

}
