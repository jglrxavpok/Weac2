package weac.compiler.resolve.structure;

import org.jglr.flows.io.IndentableWriter;
import weac.compiler.compile.PseudoInterpreter;
import weac.compiler.parse.EnumClassTypes;
import weac.compiler.precompile.insn.PrecompiledInsn;
import weac.compiler.precompile.structure.PrecompiledAnnotation;
import weac.compiler.precompile.structure.PrecompiledField;
import weac.compiler.precompile.structure.PrecompiledMethod;
import weac.compiler.resolve.ClassHierarchy;
import weac.compiler.resolve.insn.ResolvedInsn;
import weac.compiler.utils.Import;
import weac.compiler.utils.ModifierType;
import weac.compiler.utils.WeacType;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ResolvedClass {

    /**
     * The simple name of the class, such as Math, Class, String, etc.
     */
    public WeacType name;

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

    public void writeTo(IndentableWriter writer) throws IOException {
        if(!fullName.equals(name)) {
            writer.append("package ").append(fullName.substring(0, fullName.length()-name.getCoreType().getIdentifier().toString().length())).append('\n');
            writer.append("\n");
        }

        writeAnnotations(writer, annotations);

        if(isFinal)
            writer.append("final ");
        else if(isAbstract)
            writer.append("abstract ");
        else if(isMixin)
            writer.append("mixin ");
        writer.append(classType.name().toLowerCase()).append(' ').append(name.toString());
        if(parents.getSuperclass() != null) {
            writer.append(" > ").append(parents.getSuperclass().fullName);
            for (int i = 0; i < parents.getInterfaces().size(); i++) {
                if(i != parents.getInterfaces().size()-1) {
                    writer.append(" + ");
                }
                writer.append(parents.getInterfaces().get(i).fullName);
            }
        }
        writer.incrementIndentation();

        writer.append(" {\n\n");


        // TODO: Field annotations
        /*for(ResolvedField f : fields) {
            writeAnnotations(writer, f.annotations);
            writer.append(f.access.name().toLowerCase()).append(' ').append(f.type.toString()).append(' ').append(f.name.getId());
            if(!f.defaultValue.isEmpty()) {
                writer.append(" = (");
                writer.incrementIndentation();
                writeInstructions(writer, f.defaultValue);
                writer.decrementIndentation();
                writer.append(')');
            }
            writer.append(";\n\n");
        }*/

        for(ResolvedMethod m : methods) {
            writeAnnotations(writer, m.annotations);
            writer.append(m.access.name().toLowerCase()).append(' ').append(m.returnType.toString()).append(' ').append(m.name.getId());
            writer.append('(');
            for (int i = 0; i < m.argumentTypes.size(); i++) {
                if(i != 0) {
                    writer.append(", ");
                }
                writer.append(m.argumentTypes.get(i).toString()).append(' ').append(m.argumentNames.get(i).getId());
            }
            writer.append(')');
            if(!m.isAbstract) {
                writer.incrementIndentation();
                writer.append(" {\n");
                writeInstructions(writer, m.instructions);
                writer.decrementIndentation();
                writer.append("\n}");
            } else {
                writer.append(" (abstract)");
            }
            writer.append("\n");
        }

        writer.decrementIndentation();
        writer.append("\n");

        writer.append("}\n");
    }

    private void writeAnnotations(IndentableWriter writer, List<ResolvedAnnotation> annotations) throws IOException {
        for(ResolvedAnnotation a : annotations) {
            writer.append('@').append(a.getName().fullName);
            if(!a.args.isEmpty()) {
                writer.incrementIndentation();
                writer.append("(\n");
                for (int i = 0; i < a.args.size(); i++) {
                    if(i != 0)
                        writer.append(", ");
                    writeInstructions(writer, a.args.get(i));
                }
                writer.append(')');
                writer.decrementIndentation();
            }
            writer.append('\n');
        }
    }

    private void writeInstructions(IndentableWriter writer, List<ResolvedInsn> insns) throws IOException {
        for (int i = 0; i < insns.size(); i++) {
            ResolvedInsn in = insns.get(i);
            writer.write(in.toString());
            writer.write('\n');
        }
    }

    public List<ResolvedMethod> getMethods() {
        return this.methods;
    }
}
