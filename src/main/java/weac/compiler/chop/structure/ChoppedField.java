package weac.compiler.chop.structure;

import weac.compiler.code.Member;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.ModifierType;
import weac.compiler.utils.WeacType;

import java.util.Collections;
import java.util.List;

/**
 * Represents a field that has be extracted from the source code.
 */
public class ChoppedField implements Member {

    /**
     * The line in the file which holds this field
     */
    public int startingLine = -1;

    /**
     * The returnType of the field
     */
    public Identifier type;

    /**
     * The name of the field
     */
    public Identifier name;

    /**
     * The default value of this field. Not yet resolved/compiled. Only extracted
     */
    public String defaultValue;

    /**
     * The access modifier of this field
     */
    public ModifierType access = ModifierType.PUBLIC;

    public List<ChoppedAnnotation> annotations;
    public boolean isCompilerSpecial;

    /**
     * Prints this field to the console, intended for debugging
     */
    @Deprecated
    public void echo() {
        if(defaultValue != null)
            System.out.println(access.name().toLowerCase()+" "+type+" "+name+" = "+defaultValue);
        else
            System.out.println(access.name().toLowerCase()+" "+type+" "+name);
    }

    @Override
    public String getName() {
        return name.getId();
    }

    @Override
    public String getCanonicalName() {
        return getName();
    }

    @Override
    public ModifierType getAccess() {
        return access;
    }

    @Override
    public List<WeacType> getGenericParameterNames() {
        return Collections.emptyList();
    }
}
