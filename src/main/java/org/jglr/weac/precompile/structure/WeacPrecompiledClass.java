package org.jglr.weac.precompile.structure;

import org.jglr.weac.parse.EnumClassTypes;
import org.jglr.weac.parse.structure.WeacParsedField;
import org.jglr.weac.parse.structure.WeacParsedMethod;
import org.jglr.weac.utils.WeacAnnotation;
import org.jglr.weac.utils.WeacModifierType;

import java.util.List;

public class WeacPrecompiledClass {

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
    public List<WeacPrecompiledField> fields;

    /**
     * The methods defined in this class
     */
    public List<WeacPrecompiledMethod> methods;

    /**
     * The parent class, can be null. The name is not yet resolved (that's to say we don't know yet if it is a valid class)
     */
    public String motherClass;

    /**
     * The interfaces this class implements
     */
    public List<String> interfacesImplemented;

    /**
     * Empty if this class is not an enum, otherwise contains the enum constants of this class
     */
    public List<WeacPrecompiledEnumConstant> enumConstants;

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

}
