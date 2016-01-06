package org.jglr.weac.resolve.structure;

import org.jglr.weac.parse.EnumClassTypes;
import org.jglr.weac.parse.structure.WeacParsedField;
import org.jglr.weac.parse.structure.WeacParsedMethod;
import org.jglr.weac.utils.WeacAnnotation;
import org.jglr.weac.utils.WeacModifierType;

import java.util.List;

public class WeacResolvedClass {

    /**
     * The line at which the class definition starts in the source file
     */
    public int startingLine = -1;

    /**
     * The simple name of the class, such as Math, Class, String, etc.
     */
    public String name;

    /**
     * The class returnType
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
     * The parent class, can be null.
     */
    public WeacResolvedClass motherClass;

    /**
     * The interfaces this class implements
     */
    public List<WeacResolvedClass> interfacesImplemented;

    /**
     * Empty if this class is not an enum, otherwise contains the names & instantiation of each of the enum constants T'es trop fort en Anglais !
     */
    public List<String> enumConstants;

    public boolean isAbstract;

    /**
     * Is this class meant to have its code injected into the classes inheriting from it?
     */
    public boolean isMixin;

    /**
     * The access returnType to this class
     */
    public WeacModifierType access = WeacModifierType.PUBLIC;

    public List<WeacAnnotation> annotations;

    public String fullName;
}
