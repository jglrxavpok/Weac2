package org.jglrxavpok.weac.parse.structure;

import org.jglrxavpok.weac.code.WeacMember;
import org.jglrxavpok.weac.utils.Identifier;
import org.jglrxavpok.weac.utils.WeacModifierType;

import java.util.Collections;
import java.util.List;

/**
 * Represents a field that has be extracted from the source code.
 */
public class WeacParsedField implements WeacMember {

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
    public WeacModifierType access = WeacModifierType.PUBLIC;

    public List<WeacParsedAnnotation> annotations;
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
    public WeacModifierType getAccess() {
        return access;
    }

    @Override
    public List<String> getGenericParameterNames() {
        return Collections.emptyList();
    }
}
