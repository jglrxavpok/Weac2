package org.jglr.weac.resolve.structure;

import org.jglr.weac.parse.EnumClassTypes;
import org.jglr.weac.parse.structure.WeacParsedField;
import org.jglr.weac.parse.structure.WeacParsedMethod;
import org.jglr.weac.resolve.ClassParents;
import org.jglr.weac.utils.WeacAnnotation;
import org.jglr.weac.utils.WeacModifierType;

import java.util.List;

public class WeacResolvedClass {

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
    public List<WeacResolvedField> fields;

    /**
     * The methods defined in this class
     */
    public List<WeacResolvedMethod> methods;

    /**
     * The interfaces and mixins this class implements
     */
    public ClassParents parents;

    /**
     * Empty if this class is not an enum, otherwise contains the names & instantiation of each of the enum constants
     */
    public List<WeacResolvedEnumConstant> enumConstants;

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
