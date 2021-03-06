package weac.compiler.chop.structure;

import weac.compiler.code.Member;
import weac.compiler.chop.EnumClassTypes;
import weac.compiler.utils.ModifierType;
import weac.compiler.utils.WeacType;

import java.util.Collections;
import java.util.List;

/**
 * Represents a class that has be extracted from the source code.
 */
public class ChoppedClass implements Member {

    /**
     * The line at which the class definition starts in the source file
     */
    public int startingLine = -1;

    /**
     * The simple name of the class, such as Math, Class, String, etc.
     */
    public WeacType name;

    public String packageName;

    /**
     * The class returnType
     */
    public EnumClassTypes classType;

    /**
     * The fields present in this class
     */
    public List<ChoppedField> fields;

    /**
     * The methods defined in this class
     */
    public List<ChoppedMethod> methods;

    /**
     * The parent class, can be null. The name is not yet resolved (that's to say we don't know yet if it is a valid class)
     */
    public String motherClass;

    /**
     * The interfaces this class implements
     */
    public List<String> interfacesImplemented;

    /**
     * Empty if this class is not an enum, otherwise contains the names & instantiation of each of the enum constants
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
    public ModifierType access = ModifierType.PUBLIC;

    public List<ChoppedAnnotation> annotations;

    public boolean isCompilerSpecial;
    public boolean isFinal;

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
        if(!enumConstants.isEmpty()) {
            for(int i = 0;i<enumConstants.size();i++) {
                if(i != 0) {
                    System.out.print(", ");
                }
                System.out.print(enumConstants.get(i));
            }
            System.out.println(";");
            System.out.println();
        }
        fields.forEach(ChoppedField::echo);
        methods.forEach(ChoppedMethod::echo);
        System.out.println("}");
    }

    @Override
    public String getName() {
        return name.getCoreType().getIdentifier().getId();
    }

    @Override
    public String getCanonicalName() {
        return packageName == null ? getName() : packageName+"."+getName();
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
