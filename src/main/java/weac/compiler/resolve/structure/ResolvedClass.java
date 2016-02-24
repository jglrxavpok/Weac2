package weac.compiler.resolve.structure;

import weac.compiler.compile.PseudoInterpreter;
import weac.compiler.parse.EnumClassTypes;
import weac.compiler.resolve.ClassHierarchy;
import weac.compiler.resolve.insn.ResolvedInsn;
import weac.compiler.utils.ModifierType;
import weac.compiler.utils.WeacType;

import java.util.LinkedList;
import java.util.List;

public class ResolvedClass {

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
    public List<ResolvedField> fields;

    /**
     * The methods defined in this class
     */
    public List<ResolvedMethod> methods;

    /**
     * The interfaces and mixins this class implements
     */
    public ClassHierarchy parents;

    /**
     * Empty if this class is not an enum, otherwise contains the names & instantiation of each of the enum constants
     */
    public List<ResolvedEnumConstant> enumConstants;

    public boolean isAbstract;

    /**
     * Is this class meant to have its code injected into the classes inheriting from it?
     */
    public boolean isMixin;

    /**
     * The access returnType to this class
     */
    public ModifierType access = ModifierType.PUBLIC;

    public List<ResolvedAnnotation> annotations;

    public String fullName;
    public boolean isCompilerSpecial;

    public boolean isFinal;

    public ResolvedClass() {
        annotations = new LinkedList<>();
        fields = new LinkedList<>();
        methods = new LinkedList<>();
        enumConstants = new LinkedList<>();
    }

    public boolean hasField(String name, WeacType type) {
        return getField(name, type) != null;
    }

    public ResolvedField getField(String name, WeacType type) {
        if(fields == null)
            return null;
        for(ResolvedField f : fields) {
            if(f.name.getId().equals(name) && f.type.equals(type)) {
                return f;
            }
        }
        return null;
    }

    public boolean isAnnotationRuntimeVisible(PseudoInterpreter pseudoInterpreter) {
        if(classType == EnumClassTypes.ANNOTATION && hasField("__runtime", WeacType.BOOLEAN_TYPE)) {
            ResolvedField field = getField("__runtime", WeacType.BOOLEAN_TYPE);
            List<ResolvedInsn> insns = field.defaultValue;
            if(!insns.isEmpty()) {
                Object value = pseudoInterpreter.interpret(insns);
                if(value != null && value instanceof Boolean) {
                    return (boolean) value;
                } else {
                    throw new RuntimeException("Runtime visibility must be set to a boolean value");
                }
            } else {
                throw new RuntimeException("Runtime visibility cannot be unset in "+name);
            }
        }
        return classType == EnumClassTypes.ANNOTATION;
    }
}
