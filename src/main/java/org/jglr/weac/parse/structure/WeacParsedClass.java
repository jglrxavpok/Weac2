package org.jglr.weac.parse.structure;

import org.jglr.weac.parse.EnumClassTypes;
import org.jglr.weac.utils.WeacModifier;

import java.util.List;

/**
 * Represents a class that has be extracted from the source code.
 */
public class WeacParsedClass {

    /**
     * The line at which the class definition starts in the source file
     */
    public int startingLine = -1;

    /**
     * The full name of the class, e.g. weac.lang.Math
     */
    public String name;

    /**
     * The class type
     */
    public EnumClassTypes classType;

    /**
     * The fields present in this class
     */
    public List<WeacParsedField> fields;

    /**
     * The methods defined in this class
     */
    public List<WeacParsedMethod> methods;

    /**
     * The parent class, can be null. The name is not yet resolved (that's to say we don't know yet if it is a valid class)
     */
    public String motherClass;

    /**
     * The interfaces this class implements
     */
    public List<String> interfacesImplemented;

    /**
     * The access type to this class
     */
    public WeacModifier access = WeacModifier.PUBLIC;

    /**
     * Prints this class to the console, intended for debug use only
     */
    @Deprecated
    public void echo() {
        System.out.print(access.name().toLowerCase()+" "+classType.name().toLowerCase()+" "+name);
        if(motherClass != null) {
            System.out.print(" > "+motherClass);
            if(!interfacesImplemented.isEmpty())
                System.out.print(" + ");
        } else {
            if(!interfacesImplemented.isEmpty())
                System.out.print(" > ");
        }

        for(String interfaceImpl : interfacesImplemented) {
            System.out.print(interfaceImpl);
            System.out.print(" + ");
        }
        System.out.println(" {");
        fields.forEach(WeacParsedField::echo);
        methods.forEach(WeacParsedMethod::echo);
        System.out.println("}");
    }
}
