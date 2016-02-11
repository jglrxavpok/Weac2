package org.jglrxavpok.weac.precompile.structure;

import org.jglrxavpok.weac.precompile.insn.WeacPrecompiledInsn;
import org.jglrxavpok.weac.utils.Identifier;
import org.jglrxavpok.weac.utils.WeacModifierType;

import java.util.LinkedList;
import java.util.List;

public class WeacPrecompiledMethod {

    public Identifier returnType;

    public Identifier name;

    /**
     * The types of the arguments
     */
    public final List<Identifier> argumentTypes;

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

    public boolean isJavaImported;

    /**
     * The access modifier of the method
     */
    public WeacModifierType access = WeacModifierType.PUBLIC;

    public final List<WeacPrecompiledAnnotation> annotations;

    public final List<WeacPrecompiledInsn> instructions;

    public boolean isCompilerSpecial;

    public WeacPrecompiledMethod() {
        instructions = new LinkedList<>();
        annotations = new LinkedList<>();
        argumentNames = new LinkedList<>();
        argumentTypes = new LinkedList<>();
    }
}
