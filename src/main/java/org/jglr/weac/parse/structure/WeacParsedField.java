package org.jglr.weac.parse.structure;

import org.jglr.weac.utils.Identifier;
import org.jglr.weac.utils.WeacModifier;

/**
 * Represents a field that has be extracted from the source code.
 */
public class WeacParsedField {

    /**
     * The line in the file which holds this field
     */
    public int startingLine = -1;

    /**
     * The type of the field
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
    public WeacModifier access = WeacModifier.PUBLIC;

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
}
