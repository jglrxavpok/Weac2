package weac.compiler.precompile.structure;

import weac.compiler.code.Member;
import weac.compiler.parse.EnumClassTypes;
import weac.compiler.precompile.insn.PrecompiledInsn;
import org.jglr.flows.io.IndentableWriter;
import weac.compiler.utils.Import;
import weac.compiler.utils.ModifierType;
import weac.compiler.utils.WeacType;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PrecompiledClass implements Member {

    /**
     * The simple name of the class, such as Math, Class, String, etc.
     */
    public WeacType name;

    public String packageName;

    /**
     * The class returnType
     */
    public EnumClassTypes classType;

    /**
     * The fields present in this class
     */
    public final List<PrecompiledField> fields;

    /**
     * The methods defined in this class
     */
    public final List<PrecompiledMethod> methods;

    /**
     * The parent class, can be null. The name is not yet resolved (that's to say we don't know yet if it is a valid class)
     */
    public String motherClass;

    /**
     * The interfaces this class implements
     */
    public final List<String> interfacesImplemented;

    /**
     * Empty if this class is not an enum, otherwise contains the enum constants of this class
     */
    public final List<PrecompiledEnumConstant> enumConstants;

    public boolean isAbstract;

    /**
     * Is this class meant to have its code injected into the classes inheriting from it?
     */
    public boolean isMixin;

    /**
     * The access returnType to this class
     */
    public ModifierType access = ModifierType.PUBLIC;

    public final List<PrecompiledAnnotation> annotations;

    public String fullName;

    public boolean isCompilerSpecial;
    public final List<Import> imports;
    private final List<WeacType> paramNames;
    public boolean isFinal;

    public PrecompiledClass() {
        annotations = new LinkedList<>();
        interfacesImplemented = new LinkedList<>();
        enumConstants = new LinkedList<>();
        fields = new LinkedList<>();
        methods = new LinkedList<>();
        imports = new LinkedList<>();
        paramNames = new LinkedList<>();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PrecompiledClass) {
            return fullName.equals(((PrecompiledClass) obj).fullName);
        }
        return super.equals(obj);
    }

    public void writeTo(IndentableWriter writer) throws IOException {
        if(packageName != null) {
            writer.append("package ").append(packageName).append('\n');
            writer.append("\n");
        }
        for(Import im : imports) {
            writer.append("import ").append(im.importedType);
            if(im.usageName != null) {
                writer.append(" as ").write(im.usageName);
            }
            writer.append("\n");
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
        if(!getGenericParameterNames().isEmpty()) {
            writer.append('<');
            for (int i = 0; i < getGenericParameterNames().size(); i++) {
                WeacType type = getGenericParameterNames().get(i);
                if(i != 0) {
                    writer.append(", ");
                }
                writer.append(type.toString());
            }
            writer.append('>');
        }
        if(motherClass != null) {
            writer.append(" > ").append(motherClass);
            for (int i = 0; i < interfacesImplemented.size(); i++) {
                if(i != interfacesImplemented.size()-1) {
                    writer.append(" + ");
                }
                writer.append(interfacesImplemented.get(i));
            }
        }
        writer.incrementIndentation();

        writer.append(" {\n\n");


        for(PrecompiledField f : fields) {
            writeAnnotations(writer, f.annotations);
            writer.append(f.access.name().toLowerCase()).append(' ').append(f.type.getId()).append(' ').append(f.name.getId());
            if(!f.defaultValue.isEmpty()) {
                writer.append(" = (");
                writer.incrementIndentation();
                writeInstructions(writer, f.defaultValue);
                writer.decrementIndentation();
                writer.append(')');
            }
            writer.append(";\n\n");
        }

        for(PrecompiledMethod m : methods) {
            writeAnnotations(writer, m.annotations);
            writer.append(m.access.name().toLowerCase()).append(' ').append(m.returnType.getId()).append(' ').append(m.name.getId());
            writer.append('(');
            for (int i = 0; i < m.argumentTypes.size(); i++) {
                if(i != 0) {
                    writer.append(", ");
                }
                writer.append(m.argumentTypes.get(i).getId()).append(' ').append(m.argumentNames.get(i).getId());
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

    private void writeAnnotations(IndentableWriter writer, List<PrecompiledAnnotation> annotations) throws IOException {
        for(PrecompiledAnnotation a : annotations) {
            writer.append('@').append(a.getName());
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

    private void writeInstructions(IndentableWriter writer, List<PrecompiledInsn> insns) throws IOException {
        for (int i = 0; i < insns.size(); i++) {
            PrecompiledInsn in = insns.get(i);
            writer.write(in.toString());
            writer.write('\n');
        }
    }

    @Override
    public String getName() {
        return name.getCoreType().toString();
    }

    @Override
    public String getCanonicalName() {
        return fullName;
    }

    @Override
    public ModifierType getAccess() {
        return access;
    }

    @Override
    public List<WeacType> getGenericParameterNames() {
        return paramNames;
    }
}
