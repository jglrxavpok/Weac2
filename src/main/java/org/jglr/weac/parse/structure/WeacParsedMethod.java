package org.jglr.weac.parse.structure;

import org.jglr.weac.utils.Identifier;
import org.jglr.weac.utils.WeacModifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a method extracted from the source file
 */
public class WeacParsedMethod {

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
    public WeacModifier access = WeacModifier.PUBLIC;
    public int off;

    public WeacParsedMethod() {
        argumentNames = new ArrayList<>();
        argumentTypes = new ArrayList<>();
    }

    /**
     * Prints this method to the console, intended for debugging
     */
    @Deprecated
    public void echo() {
        System.out.print(access.name().toLowerCase()+" "+ returnType +" "+name+"(");
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
        }
        System.out.println("}");
    }
}
