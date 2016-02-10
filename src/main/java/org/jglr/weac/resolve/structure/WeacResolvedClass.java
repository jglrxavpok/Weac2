package org.jglr.weac.resolve.structure;

import org.jglr.weac.compile.WeacPseudoInterpreter;
import org.jglr.weac.parse.EnumClassTypes;
import org.jglr.weac.resolve.ClassHierarchy;
import org.jglr.weac.resolve.insn.ResolveOpcodes;
import org.jglr.weac.resolve.insn.WeacLoadBooleanInsn;
import org.jglr.weac.resolve.insn.WeacResolvedInsn;
import org.jglr.weac.utils.WeacModifierType;
import org.jglr.weac.utils.WeacType;

import java.util.LinkedList;
import java.util.List;

public class WeacResolvedClass {

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
    public List<WeacResolvedField> fields;

    /**
     * The methods defined in this class
     */
    public List<WeacResolvedMethod> methods;

    /**
     * The interfaces and mixins this class implements
     */
    public ClassHierarchy parents;

    /**
     * Empty if this class is not an enum, otherwise contains the names & instantiation of each of the enum constants
     */
    public List<WeacResolvedEnumConstant> enumConstants;

    public boolean isAbstract;

    /**
     * Is this class meant to have its code injected into the classes inheriting from it?
     */
    public boolean isMixin;

    /**
     * The access returnType to this class
     */
    public WeacModifierType access = WeacModifierType.PUBLIC;

    public List<WeacResolvedAnnotation> annotations;

    public String fullName;
    public boolean isCompilerSpecial;

    public WeacResolvedClass() {
        annotations = new LinkedList<>();
        fields = new LinkedList<>();
        methods = new LinkedList<>();
        enumConstants = new LinkedList<>();
    }

    public boolean hasField(String name, WeacType type) {
        return getField(name, type) != null;
    }

    public WeacResolvedField getField(String name, WeacType type) {
        if(fields == null)
            return null;
        for(WeacResolvedField f : fields) {
            if(f.name.getId().equals(name) && f.type.equals(type)) {
                return f;
            }
        }
        return null;
    }

    public boolean isAnnotationRuntimeVisible(WeacPseudoInterpreter pseudoInterpreter) {
        if(classType == EnumClassTypes.ANNOTATION && hasField("__runtime", WeacType.BOOLEAN_TYPE)) {
            WeacResolvedField field = getField("__runtime", WeacType.BOOLEAN_TYPE);
            List<WeacResolvedInsn> insns = field.defaultValue;
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
