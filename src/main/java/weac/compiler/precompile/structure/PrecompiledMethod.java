package weac.compiler.precompile.structure;

import weac.compiler.precompile.insn.PrecompiledInsn;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.ModifierType;

import java.util.LinkedList;
import java.util.List;

public class PrecompiledMethod {

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
    public ModifierType access = ModifierType.PUBLIC;

    public final List<PrecompiledAnnotation> annotations;

    public final List<PrecompiledInsn> instructions;

    public boolean isCompilerSpecial;

    public PrecompiledMethod() {
        instructions = new LinkedList<>();
        annotations = new LinkedList<>();
        argumentNames = new LinkedList<>();
        argumentTypes = new LinkedList<>();
    }
}
