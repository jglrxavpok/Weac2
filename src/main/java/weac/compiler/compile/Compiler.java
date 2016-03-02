package weac.compiler.compile;

import weac.compiler.CompileUtils;
import weac.compiler.parse.EnumClassTypes;
import weac.compiler.precompile.Label;
import weac.compiler.precompile.structure.PrecompiledClass;
import weac.compiler.resolve.insn.*;
import weac.compiler.resolve.structure.*;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.Constants;
import weac.compiler.utils.ModifierType;
import weac.compiler.utils.WeacType;
import org.objectweb.asm.*;

import java.util.*;

public class Compiler extends CompileUtils implements Opcodes {

    private final PseudoInterpreter pseudoInterpreter;

    public Compiler() {
        pseudoInterpreter = new PseudoInterpreter();
    }

    public Map<String, byte[]> process(ResolvedSource source) {
        Map<String, byte[]> compiledClasses = new HashMap<>();

        for(ResolvedClass clazz : source.classes) {
            System.out.println("COMPILING "+clazz.fullName);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            String internalName = toInternal(clazz.fullName);
            Type type = Type.getType("L"+internalName+";");

            String signature = null;
            String superclass = Type.getInternalName(Object.class);
            if(clazz.classType != EnumClassTypes.INTERFACE && clazz.classType != EnumClassTypes.ANNOTATION && !clazz.isMixin) {
               superclass = toInternal(clazz.parents.getSuperclass());
            }
            writer.visit(V1_8, convertAccessToASM(clazz), internalName, signature, superclass, convertToASM(clazz.parents.getInterfaces()));
            writer.visitSource(source.fileName, "Compiled from WeaC");

            if(clazz.classType == EnumClassTypes.OBJECT) {
                writer.visitField(ACC_PUBLIC + ACC_STATIC, Constants.SINGLETON_INSTANCE_FIELD, type.getDescriptor(), null, null);

                writeGetter(writer, true, type, type, Constants.SINGLETON_INSTANCE_FIELD);
            }

            Type primitiveType = writeClassAnnotations(clazz.annotations, clazz, type, writer);

            writeStaticBlock(writer, type, clazz);

            writeFields(writer, type, clazz, primitiveType);

            writeMethods(writer, type, clazz, primitiveType);

            writer.visitEnd();

            compiledClasses.put(clazz.fullName, writer.toByteArray());
        }
        return compiledClasses;
    }

    private Type writeClassAnnotations(List<ResolvedAnnotation> annotations, ResolvedClass clazz, Type type, ClassWriter writer) {
        Type primitiveType = null;
        for(ResolvedAnnotation annotation : annotations) {
            boolean visible = annotation.getName().isAnnotationRuntimeVisible(pseudoInterpreter);
            AnnotationVisitor av = writer.visitAnnotation(toDescriptor(new WeacType(null, annotation.getName().fullName, true)), visible);
            ResolvedClass annotClass = annotation.getName();
            annotClass.methods.forEach(m -> {
                if(m.returnType.isArray()) {
                    AnnotationVisitor arrayVisitor = av.visitArray(m.name.getId());
                    // TODO
                } else {
                    String name = m.name.getId();
                    av.visit(name, getValue(annotation.args.get(0)));
                }
            });
            av.visitEnd();

            if(annotation.getName().fullName.equals(Constants.JAVA_PRIMITIVE_ANNOTATION)) {
                Object value = getValue(annotation.getArgs().get(0));
                if(value instanceof String) {
                    switch ((String) value) {
                        case "void":
                            primitiveType = Type.VOID_TYPE;
                            break;

                        case "boolean":
                            primitiveType = Type.BOOLEAN_TYPE;
                            break;

                        case "char":
                            primitiveType = Type.CHAR_TYPE;
                            break;

                        case "byte":
                            primitiveType = Type.BYTE_TYPE;
                            break;

                        case "short":
                            primitiveType = Type.SHORT_TYPE;
                            break;

                        case "int":
                            primitiveType = Type.INT_TYPE;
                            break;

                        case "float":
                            primitiveType = Type.FLOAT_TYPE;
                            break;

                        case "long":
                            primitiveType = Type.LONG_TYPE;
                            break;

                        case "double":
                            primitiveType = Type.DOUBLE_TYPE;
                            break;
                    }
                } else {
                    newError("INVALID value TYPE", -1); // todo: line
                }
            }
        }
        return primitiveType;
    }

    private Type getPrimitiveType(WeacType type) {
        if(type.equals(WeacType.BOOLEAN_TYPE)) {
            return Type.BOOLEAN_TYPE;
        } else if(type.equals(WeacType.BYTE_TYPE)) {
            return Type.BYTE_TYPE;
        } else if(type.equals(WeacType.CHAR_TYPE)) {
            return Type.CHAR_TYPE;
        } else if(type.equals(WeacType.DOUBLE_TYPE)) {
            return Type.DOUBLE_TYPE;
        } else if(type.equals(WeacType.FLOAT_TYPE)) {
            return Type.FLOAT_TYPE;
        } else if(type.equals(WeacType.INTEGER_TYPE)) {
            return Type.INT_TYPE;
        } else if(type.equals(WeacType.LONG_TYPE)) {
            return Type.LONG_TYPE;
        } else if(type.equals(WeacType.SHORT_TYPE)) {
            return Type.SHORT_TYPE;
        } else if(type.equals(WeacType.VOID_TYPE)) {
            return Type.VOID_TYPE;
        }

        return null;
    }

    private Object getValue(List<ResolvedInsn> instructions) {
        return pseudoInterpreter.interpret(instructions);
    }

    private void writeMethods(ClassWriter writer, Type type, ResolvedClass clazz, Type primitiveType) {
        if(clazz.classType == EnumClassTypes.OBJECT && clazz.parents.hasInterface(Constants.APPLICATION_CLASS)) {
            writeMainMethod(writer, type, clazz);
        }
        int nConstructors = 0;
        boolean convertInstanceMethodToStatic = false;
        if(primitiveType != null) {
            convertInstanceMethodToStatic = true;
        }
        for(ResolvedMethod method : clazz.methods) {
            if(method.isCompilerSpecial) {
                continue;
            }
            Type returnType = toJVMType(method.returnType);
            Type methodType;
            String name;
            List<Type> argTypes = new LinkedList<>();
            if(convertInstanceMethodToStatic) {
                argTypes.add(primitiveType);
            }
            method.argumentTypes.stream()
                    .map(this::toJVMType)
                    .forEach(argTypes::add);
            Type[] args = argTypes.toArray(new Type[argTypes.size()]);
            if(method.isConstructor) {
                name = "<init>";
                methodType = Type.getMethodType(Type.VOID_TYPE, args);
            } else if(method.overloadOperator != null) {
                name = "op_"+method.overloadOperator.name().toUpperCase(); // TODO: support custom operators?
                methodType = Type.getMethodType(returnType, args);
            } else {
                methodType = Type.getMethodType(returnType, args);
                name = method.name.getId();
            }

            int access = getAccess(method.access);
            if(convertInstanceMethodToStatic) {
                access |= ACC_STATIC;
            }
            boolean isAbstract = false;
            if(method.isAbstract || clazz.isMixin || clazz.classType == EnumClassTypes.INTERFACE || clazz.classType == EnumClassTypes.ANNOTATION) {
                access |= ACC_ABSTRACT;
                isAbstract = true;
            }
            MethodVisitor mv = writer.visitMethod(access, name, methodType.getDescriptor(), null, null);
            org.objectweb.asm.Label start = new org.objectweb.asm.Label();
            org.objectweb.asm.Label end = new org.objectweb.asm.Label();

            int localIndex = 0;
            if(!convertInstanceMethodToStatic) {
                localIndex++; // 'this'
            } else {
                mv.visitParameter("__value", ACC_FINAL);
                mv.visitLocalVariable("__value", primitiveType.getDescriptor(), null, new org.objectweb.asm.Label(), new org.objectweb.asm.Label(), localIndex++);
            }
            List<Identifier> argumentNames = method.argumentNames;
            for (int i = 0; i < argumentNames.size(); i++) {
                Identifier argName = argumentNames.get(i);
                Type argType = argTypes.get(i);
                mv.visitParameter(argName.getId(), ACC_FINAL);
                mv.visitLocalVariable(argName.getId(), argType.getDescriptor(), null, new org.objectweb.asm.Label(), new org.objectweb.asm.Label(), localIndex++);
            }

            if(!isAbstract) {
                mv.visitCode();
                mv.visitLabel(start);
                if(!method.isAbstract && method.isConstructor) {
                    clazz.fields.stream()
                            .filter(f -> !f.defaultValue.isEmpty())
                            .forEach(f -> {
                                mv.visitLabel(new org.objectweb.asm.Label());
                                mv.visitVarInsn(ALOAD, 0);
                                this.compileSingleExpression(type, mv, f.defaultValue);
                                mv.visitFieldInsn(PUTFIELD, type.getInternalName(), f.name.getId(), toJVMType(f.type).getDescriptor());
                            });
                    nConstructors++;
                }
                compileSingleExpression(type, mv, method.instructions);
                mv.visitInsn(RETURN);
                mv.visitLabel(end);
                try {
                    mv.visitMaxs(0,0);
                } catch (Exception e) {
                    try {
                        throw new RuntimeException("WARNING in "+method.name+" "+method.argumentTypes.get(0)+" / "+type, e);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }

            mv.visitEnd();
        }
        if(nConstructors == 0) {
            MethodVisitor mv = writer.visitMethod(ACC_PRIVATE, "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), null, null);
            org.objectweb.asm.Label start = new org.objectweb.asm.Label();
            org.objectweb.asm.Label end = new org.objectweb.asm.Label();

            mv.visitCode();

            int localIndex = 1; // 'this'
            mv.visitLabel(start);
            clazz.fields.stream()
                    .filter(f -> !f.defaultValue.isEmpty())
                    .forEach(f -> {
                        mv.visitLabel(new org.objectweb.asm.Label());
                        mv.visitVarInsn(ALOAD, 0);
                        this.compileSingleExpression(type, mv, f.defaultValue);
                        mv.visitFieldInsn(PUTFIELD, type.getInternalName(), f.name.getId(), toJVMType(f.type).getDescriptor());
                    });
            mv.visitInsn(RETURN);
            mv.visitLabel(end);
            mv.visitMaxs(0,0);
            mv.visitEnd();
        }
    }

    private void writeMainMethod(ClassWriter writer, Type type, ResolvedClass clazz) {
        String mainDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String[].class));
        MethodVisitor mv = writer.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", mainDesc, null, null);
        mv.visitCode();
        mv.visitLabel(new org.objectweb.asm.Label());
        mv.visitTypeInsn(NEW, type.getInternalName());
        mv.visitInsn(DUP);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, type.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false); // call constructor

        mv.visitMethodInsn(INVOKEINTERFACE, type.getInternalName(), "start", mainDesc, true); // call Application::start(String[])
        mv.visitLabel(new org.objectweb.asm.Label());
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void compileSingleExpression(Type type, MethodVisitor writer, List<ResolvedInsn> l) {
        System.out.println("=== INSNS "+type.getInternalName()+" ===");
        l.forEach(System.out::println);
        System.out.println("=============");
        ResolvedInsn last = null;
        for (int i = 0; i < l.size(); i++) {
            ResolvedInsn insn = l.get(i);
            last = insn;
            if(insn instanceof LoadByteInsn) {
                byte n = ((LoadByteInsn) insn).getNumber();
                writer.visitLdcInsn(n);
            } else if(insn instanceof LoadDoubleInsn) {
                double n = ((LoadDoubleInsn) insn).getNumber();
                writer.visitLdcInsn(n);
            } else if(insn instanceof LoadFloatInsn) {
                float n = ((LoadFloatInsn) insn).getNumber();
                writer.visitLdcInsn(n);
            } else if(insn instanceof LoadIntInsn) {
                int n = ((LoadIntInsn) insn).getNumber();
                writer.visitLdcInsn(n);
            } else if(insn instanceof LoadLongInsn) {
                long n = ((LoadLongInsn) insn).getNumber();
                writer.visitLdcInsn(n);
            } else if(insn instanceof LoadShortInsn) {
                short n = ((LoadShortInsn) insn).getNumber();
                writer.visitLdcInsn(n);
            } else if(insn instanceof LoadCharInsn) {
                char n = ((LoadCharInsn) insn).getNumber();
                writer.visitLdcInsn(n);
            } else if(insn instanceof LoadStringInsn) {
                String n = ((LoadStringInsn) insn).getValue();
                writer.visitLdcInsn(n);
            } else if(insn instanceof LoadBooleanInsn) {
                boolean b = ((LoadBooleanInsn) insn).getValue();
                writer.visitInsn(b ? ICONST_1 : ICONST_0);
            } else if(insn instanceof ResolvedLabelInsn) {
                Label b = ((ResolvedLabelInsn) insn).getLabel();
                writer.visitLabel(new org.objectweb.asm.Label());
            } else if(insn.getOpcode() >= ResolveOpcodes.FIRST_RETURN_OPCODE && insn.getOpcode() <= ResolveOpcodes.LAST_RETURN_OPCODE) {
                int code = insn.getOpcode();
                int returnIndex = code - ResolveOpcodes.FIRST_RETURN_OPCODE;
                int[] returnOpcodes = new int[] {RETURN, ARETURN, IRETURN, FRETURN, IRETURN, LRETURN, IRETURN, IRETURN, DRETURN};
                writer.visitInsn(returnOpcodes[returnIndex]);
            } else if(insn instanceof StoreVarInsn) {
                StoreVarInsn varInsn = (StoreVarInsn)insn;
                int localIndex = varInsn.getLocalIndex();
                writer.visitVarInsn(ASTORE, localIndex);
            } else if(insn instanceof StoreFieldInsn) {
                StoreFieldInsn fieldInsn = (StoreFieldInsn)insn;
                int opcode;
                if(fieldInsn.isStatic()) {
                    opcode = PUTSTATIC;
                } else {
                    opcode = PUTFIELD;
                }
                writer.visitFieldInsn(opcode, type.getInternalName(), fieldInsn.getName(), toDescriptor(fieldInsn.getType()));
            } else if(insn instanceof LoadFieldInsn) {
                LoadFieldInsn fieldInsn = (LoadFieldInsn)insn;
                writer.visitFieldInsn(fieldInsn.isStatic() ? GETSTATIC : GETFIELD, toJVMType(fieldInsn.getOwner()).getInternalName(), fieldInsn.getFieldName(), toDescriptor(fieldInsn.getType()));
            } else if(insn instanceof LoadVariableInsn) {
                LoadVariableInsn variableInsn = (LoadVariableInsn)insn;
                writer.visitVarInsn(ALOAD, variableInsn.getVarIndex());
            } else if(insn instanceof LoadNullInsn) {
                writer.visitInsn(ACONST_NULL);
            } else if(insn instanceof PopInsn) {
                writer.visitInsn(POP);
            } else if(insn instanceof FunctionCallInsn) {
                FunctionCallInsn callInsn = (FunctionCallInsn)insn;
                Type[] argTypes = new Type[callInsn.getArgCount()];
                for(int i0 = 0;i0<callInsn.getArgCount();i0++) {
                    argTypes[i0] = toJVMType(callInsn.getArgTypes()[i0]);
                }
                int invokeType = INVOKEVIRTUAL;
                if(callInsn.isStatic()) {
                    invokeType = INVOKESTATIC;
                }
                WeacType owner = callInsn.getOwner();
                if(owner.getSuperType() != null) {
                    if(owner.getSuperType().equals(WeacType.PRIMITIVE_TYPE)) {
                        String methodDesc = Type.getMethodDescriptor(toJVMType(callInsn.getReturnType()), toJVMType(owner, false));
                        writer.visitMethodInsn(INVOKESTATIC, toJVMType(owner, true).getInternalName(), callInsn.getName(), methodDesc, false);
                    } else {
                        String methodDesc = Type.getMethodDescriptor(toJVMType(callInsn.getReturnType()), argTypes);
                        writer.visitMethodInsn(invokeType, toJVMType(owner, true).getInternalName(), callInsn.getName(), methodDesc, false);
                    }
                } else {
                    String methodDesc = Type.getMethodDescriptor(toJVMType(callInsn.getReturnType()), argTypes);
                    writer.visitMethodInsn(invokeType, toJVMType(owner, true).getInternalName(), callInsn.getName(), methodDesc, false);
                }
            } else {
                System.err.println("unknown: "+insn);
            }
        }
    }

    private void writeFields(ClassWriter writer, Type type, ResolvedClass clazz, Type primitiveType) {
        for(ResolvedField field : clazz.fields) {
            if(field.isCompilerSpecial)
                continue;
            if(primitiveType != null) {
                newError("PrimitiveLike class cannot have fields", -1);
                break;
            }
            String desc = toJVMType(field.type).getDescriptor();
            int acc = getAccess(field.access);
            writer.visitField(acc, field.name.getId(), desc, null, null);
        }
    }

    private int getAccess(ModifierType access) {
        int acc = 0;
        switch (access) {
            case PRIVATE:
                acc = ACC_PRIVATE;
                break;

            case PROTECTED:
                acc = ACC_PROTECTED;
                break;

            case PUBLIC:
                acc = ACC_PUBLIC;
                break;
        }
        return acc;
    }

    private void writeGetter(ClassWriter writer, boolean isStatic, Type owner, Type fieldType, String fieldName) {
        String getterName = "get"+Character.toUpperCase(fieldName.charAt(0))+fieldName.substring(1);
        MethodVisitor mv = writer.visitMethod(ACC_PUBLIC + (isStatic ? ACC_STATIC : 0), getterName, Type.getMethodDescriptor(fieldType), null, null);
        mv.visitCode();
        mv.visitLabel(new org.objectweb.asm.Label());
        if(!isStatic) {
            mv.visitVarInsn(ALOAD, 0); // load 'this'
        }
        mv.visitFieldInsn(isStatic ? GETSTATIC : GETFIELD, owner.getInternalName(), fieldName, fieldType.getDescriptor());
        mv.visitInsn(ARETURN);

        mv.visitMaxs(0,0);
        mv.visitEnd();
    }

    private void writeStaticBlock(ClassWriter writer, Type type, ResolvedClass clazz) {
        MethodVisitor mv = writer.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        if(clazz.classType == EnumClassTypes.OBJECT) {
            mv.visitLabel(new org.objectweb.asm.Label());
            mv.visitTypeInsn(NEW, type.getInternalName());
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, type.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
            // store it in the field
            mv.visitFieldInsn(PUTSTATIC, type.getInternalName(), Constants.SINGLETON_INSTANCE_FIELD, type.getDescriptor());
        }
        mv.visitLabel(new org.objectweb.asm.Label());
        mv.visitInsn(RETURN);
        mv.visitMaxs(0,0);
        mv.visitEnd();
    }

    private String toInternal(PrecompiledClass clazz) {
        if(clazz == null)
            return null;
        return toInternal(clazz.fullName);
    }

    private String[] convertToASM(List<PrecompiledClass> interfaces) {
        String[] interfaceArray = new String[interfaces.size()];

        int index = 0;
        for(PrecompiledClass clazz : interfaces) {
            interfaceArray[index++] = toInternal(clazz.fullName);
        }

        return interfaceArray;
    }

    private Type toJVMType(WeacType type) {
        return toJVMType(type, false);
    }

    private Type toJVMType(WeacType type, boolean isFunctionOwner) {
        if(!isFunctionOwner) {
            Type primitiveType = getPrimitiveType(type);
            if(primitiveType != null)
                return primitiveType;
        }
        return Type.getType(toDescriptor(type));
    }

    private String toDescriptor(WeacType type) {
        if(type.equals(WeacType.VOID_TYPE)) {
            return "V";
        }
        if(!type.isValid()) {
            System.err.println("INV: "+type.getIdentifier());
            return "I";
        }
        StringBuilder builder = new StringBuilder();
        if(type.isArray()) {
            builder.append("[");
            builder.append(toDescriptor(type.getArrayType()));
        } else {
            builder.append("L").append(type.getIdentifier().getId()).append(";");
        }
        return builder.toString().replace(".", "/");
    }

    private String toInternal(String fullName) {
        return fullName.replace(".", "/");
    }

    private int convertAccessToASM(ResolvedClass clazz) {
        int access;
        int type = 0;
        int abstractness = 0;
        switch (clazz.access) {
            case PRIVATE:
                access = ACC_PRIVATE;
                break;

            case PROTECTED:
                access = ACC_PROTECTED;
                break;

            case PUBLIC:
                access = ACC_PUBLIC;
                break;

            default:
                access = -1;
                break;
        }

        switch (clazz.classType) {
            case ANNOTATION:
                type = ACC_ANNOTATION + ACC_INTERFACE;
                break;

            case INTERFACE:
                type = ACC_INTERFACE;
                break;

            case ENUM:
                type = ACC_ENUM;
                break;

            case OBJECT:
            case STRUCT:
                type = ACC_FINAL;
                // TODO: Use something else?
                break;

            case CLASS:
                break;
        }

        if(clazz.isAbstract) {
            abstractness = ACC_ABSTRACT;
        }

        if(clazz.isFinal) {
            abstractness = ACC_FINAL;
        }

        if(clazz.isMixin) {
            type = ACC_INTERFACE;
        }

        return access | type | abstractness;
    }

}