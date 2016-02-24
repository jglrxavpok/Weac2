package weac.compiler.resolve.structure;

import weac.compiler.resolve.insn.WeacResolvedInsn;
import weac.compiler.utils.EnumOperators;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.WeacModifierType;
import weac.compiler.utils.WeacType;

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

    public final List<WeacResolvedAnnotation> annotations;

    public final List<WeacResolvedInsn> instructions;

    public boolean isCompilerSpecial;
    public EnumOperators overloadOperator;

    public WeacResolvedMethod() {
        annotations = new LinkedList<>();
        argumentNames = new LinkedList<>();
        argumentTypes = new LinkedList<>();
        instructions = new LinkedList<>();
    }
}
