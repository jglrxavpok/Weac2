package weac.compiler.parse.structure;

import weac.compiler.code.WeacMember;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.WeacModifierType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a method extracted from the source file
 */
public class WeacParsedMethod implements WeacMember {

    /**
     * The line at which the method starts
     */
    public int startingLine = -1;

    /**
     * The return returnType of the method
     */
    public Identifier returnType;

    /**
     * The name of the method
     */
    public Identifier name;

    /**
     * The types of the arguments
     */
    public List<Identifier> argumentTypes;

    /**
     * The names of the arguments
     */
    public List<Identifier> argumentNames;

    /**
     * The code of the method
     */
    public String methodSource;

    /**
     * Is this method abstract?
     */
    public boolean isAbstract;

    /**
     * Is this method a constructor?
     */
    public boolean isConstructor;

    /**
     * The access modifier of the method
     */
    public WeacModifierType access = WeacModifierType.PUBLIC;

    public List<WeacParsedAnnotation> annotations;

    public int off;
    public boolean isCompilerSpecial;

    public WeacParsedMethod() {
        argumentNames = new ArrayList<>();
        argumentTypes = new ArrayList<>();
    }

    /**
     * Prints this method to the console, intended for debugging
     */
    @Deprecated
    public void echo() {
        System.out.print(access.name().toLowerCase()+" ");
        if(isAbstract)
            System.out.print("abstract ");
        System.out.print(returnType +" "+name+"(");
        for(int i = 0;i<argumentNames.size();i++) {
            if(i != 0) {
                System.out.print(", ");
            }
            if(!isConstructor) {
                System.out.print(argumentTypes.get(i));
                System.out.print(" ");
            }
            System.out.print(argumentNames.get(i));
        }
        System.out.print(")");
        if(isAbstract) {
            System.out.println(";");
        } else {
            System.out.println(" {");
            System.out.println(methodSource);
            System.out.println("}");
        }
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
