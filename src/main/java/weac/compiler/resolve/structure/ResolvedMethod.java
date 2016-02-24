package weac.compiler.resolve.structure;

import weac.compiler.resolve.insn.ResolvedInsn;
import weac.compiler.utils.EnumOperators;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.ModifierType;
import weac.compiler.utils.WeacType;

import java.util.LinkedList;
import java.util.List;

public class ResolvedMethod {

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
    public ModifierType access = ModifierType.PUBLIC;

    public final List<ResolvedAnnotation> annotations;

    public final List<ResolvedInsn> instructions;

    public boolean isCompilerSpecial;
    public EnumOperators overloadOperator;

    public ResolvedMethod() {
        annotations = new LinkedList<>();
        argumentNames = new LinkedList<>();
        argumentTypes = new LinkedList<>();
        instructions = new LinkedList<>();
    }
}
