package org.jglr.weac.resolve.structure;

import org.jglr.weac.utils.Identifier;
import org.jglr.weac.utils.WeacAnnotation;
import org.jglr.weac.utils.WeacModifierType;
import org.jglr.weac.utils.WeacType;

import java.util.LinkedList;
import java.util.List;

public class WeacResolvedMethod {

    public WeacType returnType;

    public Identifier name;

    /**
     * The types of the arguments
     */
    public final List<WeacType> argumentTypes;

    /**
     * The names of the arguments
     */
    public final List<Identifier> argumentNames;

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

    public final List<WeacAnnotation> annotations;

    public WeacResolvedMethod() {
        annotations = new LinkedList<>();
        argumentNames = new LinkedList<>();
        argumentTypes = new LinkedList<>();
    }
}
