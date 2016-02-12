package org.jglrxavpok.weac.precompile.structure;

import org.jglrxavpok.weac.parse.EnumClassTypes;
import org.jglrxavpok.weac.utils.WeacImport;
import org.jglrxavpok.weac.utils.WeacModifierType;

import java.util.LinkedList;
import java.util.List;

public class WeacPrecompiledClass {

    /**
     * The simple name of the class, such as Math, Class, String, etc.
     */
    public String name;

    public String packageName;

    /**
     * The class returnType
     */
    public EnumClassTypes classType;

    /**
     * The fields present in this class
     */
    public final List<WeacPrecompiledField> fields;

    /**
     * The methods defined in this class
     */
    public final List<WeacPrecompiledMethod> methods;

    /**
     * The parent class, can be null. The name is not yet resolved (that's to say we don't know yet if it is a valid class)
     */
    public String motherClass;

    /**
     * The interfaces this class implements
     */
    public final List<String> interfacesImplemented;

    /**
     * Empty if this class is not an enum, otherwise contains the enum constants of this class
     */
    public final List<WeacPrecompiledEnumConstant> enumConstants;

    public boolean isAbstract;

    /**
     * Is this class meant to have its code injected into the classes inheriting from it?
     */
    public boolean isMixin;

    /**
     * The access returnType to this class
     */
    public WeacModifierType access = WeacModifierType.PUBLIC;

    public final List<WeacPrecompiledAnnotation> annotations;

    public String fullName;

    public boolean isCompilerSpecial;
    public List<WeacImport> imports;
    public boolean isFinal;

    public WeacPrecompiledClass() {
        annotations = new LinkedList<>();
        interfacesImplemented = new LinkedList<>();
        enumConstants = new LinkedList<>();
        fields = new LinkedList<>();
        methods = new LinkedList<>();
        imports = new LinkedList<>();
    }
}
