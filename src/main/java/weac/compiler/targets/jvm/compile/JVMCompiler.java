package weac.compiler.targets.jvm.compile;

import weac.compiler.CompileUtils;
import weac.compiler.chop.EnumClassTypes;
import weac.compiler.precompile.Label;
import weac.compiler.precompile.structure.PrecompiledClass;
import weac.compiler.resolve.ConstructorInfos;
import weac.compiler.resolve.insn.*;
import weac.compiler.resolve.structure.*;
import weac.compiler.resolve.values.VariableValue;
import weac.compiler.targets.TargetCompiler;
import weac.compiler.targets.jvm.JVMConstants;
import weac.compiler.targets.jvm.JVMWeacTypes;
import weac.compiler.targets.jvm.resolve.insn.BytecodeSequencesInsn;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.Constants;
import weac.compiler.utils.ModifierType;
import weac.compiler.utils.WeacType;
import org.objectweb.asm.*;

import java.util.*;

public class JVMCompiler extends CompileUtils implements Opcodes, TargetCompiler {

    private final PseudoInterpreter pseudoInterpreter;
    private final HashMap<Integer, Integer> startingOpcodes;
    private PrimitiveCastCompiler primitiveCastCompiler;

    public JVMCompiler() {
        pseudoInterpreter = new PseudoInterpreter();
        primitiveCastCompiler = new PrimitiveCastCompiler();
        startingOpcodes = new HashMap<>();
        startingOpcodes.put(ResolveOpcodes.ADD, IADD);
        startingOpcodes.put(ResolveOpcodes.MULTIPLY, IMUL);
        startingOpcodes.put(ResolveOpcodes.DIVIDE, IDIV);
        startingOpcodes.put(ResolveOpcodes.SUBTRACT, ISUB);
        startingOpcodes.put(ResolveOpcodes.MODULUS, IREM);
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
            if(clazz.classType == EnumClassTypes.ENUM) {
                superclass = Type.getInternalName(Enum.class);
            } else if(clazz.classType != EnumClassTypes.INTERFACE && clazz.classType != EnumClassTypes.ANNOTATION && !clazz.isMixin) {
               superclass = toInternal(clazz.parents.getSuperclass());
            }
            writer.visit(V1_6, convertAccessToASM(clazz), internalName, signature, superclass, convertToASM(clazz.parents.getInterfaces()));
            writer.visitSource(source.fileName, "Compiled from WeaC");

            if(clazz.classType == EnumClassTypes.OBJECT) {
                writer.visitField(ACC_PUBLIC | ACC_STATIC, Constants.SINGLETON_INSTANCE_FIELD, type.getDescriptor(), null, null);

                writeGetter(writer, true, type, type, Constants.SINGLETON_INSTANCE_FIELD);
            }

            Type primitiveType = writeClassAnnotations(clazz.annotations, clazz, type, writer);

            if(clazz.classType == EnumClassTypes.ENUM)
                writeEnumConstants(writer, type, clazz, primitiveType);

            writeStaticBlock(writer, type, clazz);

            writeFields(writer, type, clazz, primitiveType);

            writeMethods(writer, type, clazz, primitiveType);

            writer.visitEnd();

            compiledClasses.put(clazz.fullName, writer.toByteArray());
        }
        return compiledClasses;
    }

    private void writeEnumConstants(ClassWriter writer, Type type, ResolvedClass clazz, Type primitiveType) {
        clazz.enumConstants.forEach(constant -> {
            writer.visitField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, constant.name, type.getDescriptor(), null, null);
        });

        writer.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "$VALUES", "["+type.getDescriptor(), null, null);

        Type objType = Type.getType(Object.class);
        Type arrayType = Type.getType("["+type.getDescriptor());
        MethodVisitor mv = writer.visitMethod(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, "values", "()["+type.getDescriptor(), null, null);
        mv.visitCode();
        mv.visitLocalVariable("copy", "["+type.getDescriptor(), null, new org.objectweb.asm.Label(), new org.objectweb.asm.Label(), 0);
        mv.visitFieldInsn(GETSTATIC, type.getInternalName(), "$VALUES", "["+type.getDescriptor());
        mv.visitMethodInsn(INVOKEVIRTUAL, arrayType.getInternalName(), "clone", "()"+objType.getDescriptor(), false);
        mv.visitTypeInsn(CHECKCAST, arrayType.getInternalName());
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = writer.visitMethod(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, "valueOf", "(Ljava/lang/String;)"+type.getDescriptor(), null, null);
        mv.visitCode();
        mv.visitLdcInsn(type);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, "Ljava/lang/Enum", "valueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;", false);
        mv.visitTypeInsn(CHECKCAST, type.getInternalName());
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
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

            if(annotation.getName().fullName.equals(JVMConstants.JAVA_PRIMITIVE_ANNOTATION)) {
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

    public static Type getPrimitiveType(WeacType type) {
        if(type.equals(JVMWeacTypes.BOOLEAN_TYPE)) {
            return Type.BOOLEAN_TYPE;
        } else if(type.equals(JVMWeacTypes.BYTE_TYPE)) {
            return Type.BYTE_TYPE;
        } else if(type.equals(JVMWeacTypes.CHAR_TYPE)) {
            return Type.CHAR_TYPE;
        } else if(type.equals(JVMWeacTypes.DOUBLE_TYPE)) {
            return Type.DOUBLE_TYPE;
        } else if(type.equals(JVMWeacTypes.FLOAT_TYPE)) {
            return Type.FLOAT_TYPE;
        } else if(type.equals(JVMWeacTypes.INTEGER_TYPE)) {
            return Type.INT_TYPE;
        } else if(type.equals(JVMWeacTypes.LONG_TYPE)) {
            return Type.LONG_TYPE;
        } else if(type.equals(JVMWeacTypes.SHORT_TYPE)) {
            return Type.SHORT_TYPE;
        } else if(type.equals(JVMWeacTypes.VOID_TYPE)) {
            return Type.VOID_TYPE;
        }

        return null;
    }

    private Object getValue(List<ResolvedInsn> instructions) {
        return pseudoInterpreter.interpret(instructions);
    }

    private void writeMethods(ClassWriter writer, Type type, ResolvedClass clazz, Type primitiveType) {
        if(clazz.classType == EnumClassTypes.OBJECT && clazz.parents.hasInterface(JVMConstants.APPLICATION_CLASS)) {
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
            Type returnType = method.isConstructor ? Type.VOID_TYPE : toJVMType(method.returnType);
            Type methodType;
            String name;
            List<Type> argTypes = new LinkedList<>();
            if(convertInstanceMethodToStatic) {
                argTypes.add(primitiveType);
            }
            if(clazz.classType == EnumClassTypes.ENUM) {
                argTypes.add(0, Type.getType(String.class));
                argTypes.add(1, Type.INT_TYPE);
            }
            method.argumentTypes.stream()
                    .map(JVMCompiler::toJVMType)
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
            boolean isAbstract = method.isAbstract;
            if(method.isAbstract || clazz.isMixin || clazz.classType == EnumClassTypes.INTERFACE || clazz.classType == EnumClassTypes.ANNOTATION) {
                access |= ACC_ABSTRACT;
                isAbstract = true;
            }
            MethodVisitor mv = writer.visitMethod(access, name, methodType.getDescriptor(), methodType.getDescriptor(), null);
            org.objectweb.asm.Label start = new org.objectweb.asm.Label();
            org.objectweb.asm.Label end = new org.objectweb.asm.Label();

            int varIndexOffset = 0;
            if(clazz.classType == EnumClassTypes.ENUM) {
                mv.visitParameter("$name", ACC_SYNTHETIC);
                mv.visitParameter("$ordinal", ACC_SYNTHETIC);
                mv.visitLocalVariable("$name", "Ljava/lang/String;", null, new org.objectweb.asm.Label(), new org.objectweb.asm.Label(), 1);
                mv.visitLocalVariable("$ordinal", "I", null, new org.objectweb.asm.Label(), new org.objectweb.asm.Label(), 2);
                varIndexOffset += 2;
            }

            if(method.isConstructor && !hasConstructorCall(clazz, method)) {
                mv.visitVarInsn(ALOAD, 0);
                if(clazz.classType == EnumClassTypes.ENUM) {
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitVarInsn(ILOAD, 2);
                    mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Enum.class), "<init>", "(Ljava/lang/String;I)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESPECIAL, toInternal(clazz.parents.getSuperclass()), "<init>", "()V", false);
                }
            }

            int localIndex = 0;
            if(!convertInstanceMethodToStatic) {
                mv.visitLocalVariable("this", type.getDescriptor(), null, new org.objectweb.asm.Label(), new org.objectweb.asm.Label(), 0);
                localIndex++; // 'this'
            } else {
                mv.visitLocalVariable("__value", primitiveType.getDescriptor(), null, new org.objectweb.asm.Label(), new org.objectweb.asm.Label(), 0);
                mv.visitParameter("__value", ACC_FINAL);
                localIndex++;
                varIndexOffset++;
            }


            List<Identifier> argumentNames = method.argumentNames;
            for (int i = 0; i < argumentNames.size(); i++) {
                Identifier argName = argumentNames.get(i);
                Type argType = argTypes.get(i);
                mv.visitParameter(argName.getId(), ACC_MANDATED);
            //    mv.visitLocalVariable(argName.getId(), argType.getDescriptor(), null, new org.objectweb.asm.Label(), new org.objectweb.asm.Label(), localIndex++);
            }

            ArrayList<String> registredLocals = new ArrayList<>();

            if(!isAbstract) {
                mv.visitCode();

                final int finalVarIndexOffset = varIndexOffset;
                if (!method.isAbstract && method.isConstructor) {
                    clazz.fields.stream()
                            .filter(f -> !f.defaultValue.isEmpty())
                            .filter(f -> !(f.defaultValue.size() == 1 && f.defaultValue.get(0) instanceof LocalVariableTableInsn))
                            .forEach(f -> {
                                mv.visitLabel(new org.objectweb.asm.Label());
                                mv.visitVarInsn(ALOAD, 0);
                                this.compileSingleExpression(type, mv, f.defaultValue, start, end, finalVarIndexOffset, registredLocals);
                                mv.visitFieldInsn(PUTFIELD, type.getInternalName(), f.name.getId(), toJVMType(f.type).getDescriptor());
                            });
                    nConstructors++;
                }
                mv.visitLabel(start);
                try {
                    compileSingleExpression(type, mv, method.instructions, start, end, varIndexOffset, registredLocals);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                mv.visitLabel(end);
                try {
                    mv.visitMaxs(0, 0);
                } catch (Exception e) {
                    try {
                        throw new RuntimeException("WARNING in " + method.name + " " + Arrays.toString(method.argumentTypes.toArray()) + " / " + type, e);
                    } catch (Exception e1) {
                        e.printStackTrace();
                        e1.printStackTrace();
                    }
                }
                mv.visitInsn(RETURN);
            }
            mv.visitEnd();
        }
        if(nConstructors == 0) {
            if(clazz.isMixin || clazz.classType == EnumClassTypes.INTERFACE || clazz.classType == EnumClassTypes.ANNOTATION) {
                return;
            }
            MethodVisitor mv = writer.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodType(Type.VOID_TYPE).getDescriptor(), null, null);
            org.objectweb.asm.Label start = new org.objectweb.asm.Label();
            org.objectweb.asm.Label end = new org.objectweb.asm.Label();

            mv.visitCode();

            int localIndex = 1; // 'this'
            mv.visitLabel(start);
            if(!clazz.isMixin && clazz.classType != EnumClassTypes.INTERFACE && clazz.classType != EnumClassTypes.ANNOTATION) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, toInternal(clazz.parents.getSuperclass()), "<init>", "()V", false);
            }
            clazz.fields.stream()
                    .filter(f -> !f.defaultValue.isEmpty())
                    .filter(f -> !(f.defaultValue.size() == 1 && f.defaultValue.get(0) instanceof LocalVariableTableInsn))
                    .forEach(f -> {
                        mv.visitLabel(new org.objectweb.asm.Label());
                        mv.visitVarInsn(ALOAD, 0);
                        this.compileSingleExpression(type, mv, f.defaultValue, start, end, 0, new ArrayList<>());
                        mv.visitFieldInsn(PUTFIELD, type.getInternalName(), f.name.getId(), toJVMType(f.type).getDescriptor());
                    });
            mv.visitLabel(end);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0,0);
            mv.visitEnd();
        }
    }

    private boolean hasConstructorCall(ResolvedClass owner, ResolvedMethod method) {
        for (ResolvedInsn i : method.instructions) {
            if(i.getOpcode() == ResolveOpcodes.FUNCTION_CALL) {
                FunctionCallInsn callInsn = (FunctionCallInsn) i;
                if(callInsn.getOwner().getIdentifier().getId().equals(owner.fullName) && callInsn.getName().equals("<init>"))
                    return true;
            }
        }
        return false;
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

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, type.getInternalName(), "start", mainDesc, false); // call Application::start(String[])
        mv.visitLabel(new org.objectweb.asm.Label());
        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 0);
        mv.visitEnd();
    }

    private void compileSingleExpression(Type type, MethodVisitor writer, List<ResolvedInsn> l, org.objectweb.asm.Label start, org.objectweb.asm.Label end, int varIndexOffset, List<String> registredLocals) {
        System.out.println("=== INSNS "+type.getInternalName()+" ===");
        l.forEach(System.out::println);
        System.out.println("=============");
        ResolvedInsn last = null;
        LabelMap labelMap = new LabelMap();
        for (int i = 0; i < l.size(); i++) {
            ResolvedInsn insn = l.get(i);
            compileInstruction(insn, labelMap, writer, start, end, varIndexOffset, registredLocals);
        }
    }

    private void compileInstruction(ResolvedInsn insn, LabelMap labelMap, MethodVisitor writer, org.objectweb.asm.Label start, org.objectweb.asm.Label end, int varIndexOffset, List<String> registredLocals) {
        org.objectweb.asm.Label currentLabel = start;
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
            if(b.getIndex() != -1) { // TODO: better fix?
                org.objectweb.asm.Label lbl = labelMap.get(b);
                writer.visitLabel(lbl);
                currentLabel = lbl;
            }
        } else if(insn.getOpcode() >= ResolveOpcodes.FIRST_RETURN_OPCODE && insn.getOpcode() <= ResolveOpcodes.LAST_RETURN_OPCODE) {
            int code = insn.getOpcode();
            int returnIndex = code - ResolveOpcodes.FIRST_RETURN_OPCODE;
            int[] returnOpcodes = new int[] {RETURN, ARETURN, IRETURN, FRETURN, IRETURN, LRETURN, IRETURN, IRETURN, DRETURN, IRETURN};
            writer.visitInsn(returnOpcodes[returnIndex]);
        } else if(insn instanceof StoreVarInsn) {
            StoreVarInsn varInsn = (StoreVarInsn)insn;
            int localIndex = varInsn.getLocalIndex();
            WeacType weacType = varInsn.getVarType();
            Type primitive = getPrimitiveType(weacType);
            if(primitive != null) {
                writer.visitVarInsn(primitive.getOpcode(ISTORE), localIndex+(localIndex == 0 ? 0 : varIndexOffset));
            } else {
                writer.visitVarInsn(ASTORE, localIndex+(localIndex == 0 ? 0 : varIndexOffset));
            }
        } else if(insn instanceof StoreFieldInsn) {
            StoreFieldInsn fieldInsn = (StoreFieldInsn)insn;
            int opcode;
            if(fieldInsn.isStatic()) {
                opcode = PUTSTATIC;
            } else {
                opcode = PUTFIELD;
            }
            writer.visitFieldInsn(opcode, toJVMType(fieldInsn.getOwner()).getInternalName(), fieldInsn.getName(), toDescriptor(fieldInsn.getType()));
        } else if(insn instanceof LoadFieldInsn) {
            LoadFieldInsn fieldInsn = (LoadFieldInsn)insn;
            writer.visitFieldInsn(fieldInsn.isStatic() ? GETSTATIC : GETFIELD, toJVMType(fieldInsn.getOwner()).getInternalName(), fieldInsn.getFieldName(), toDescriptor(fieldInsn.getType()));
        } else if(insn instanceof LoadVariableInsn) {
            LoadVariableInsn variableInsn = (LoadVariableInsn)insn;
            Type primitiveType = getPrimitiveType(variableInsn.getVarType());
            if(primitiveType != null) {
                writer.visitVarInsn(primitiveType.getOpcode(ILOAD), variableInsn.getVarIndex()+(variableInsn.getVarIndex() == 0 ? 0 : varIndexOffset));
            } else {
                writer.visitVarInsn(ALOAD, variableInsn.getVarIndex()+(variableInsn.getVarIndex() == 0 ? 0 : varIndexOffset));
            }
        } else if(insn instanceof CompareInsn) {
            CompareInsn compInsn = (CompareInsn)insn;
            Type primitiveType = getPrimitiveType(compInsn.getResultType());
            if(primitiveType != null) {
                int loadType = -1;
                if(primitiveType == Type.DOUBLE_TYPE) {
                    writer.visitInsn(DCONST_0);
                    loadType = DCMPL;
                } else if(primitiveType == Type.FLOAT_TYPE) {
                    writer.visitInsn(FCONST_0);
                    loadType = FCMPL;
                } else if(primitiveType == Type.LONG_TYPE) {
                    writer.visitInsn(LCONST_0);
                    loadType = LCMP;
                }
                writer.visitInsn(loadType);
            } else {
                // TODO
                // invalid
            }
        } else if(insn.getOpcode() >= ResolveOpcodes.LESS && insn.getOpcode() <= ResolveOpcodes.GREATER_OR_EQUAL) {
            int[] opcodes = {IFLT, IFLE, IFGT, IFGE};
            int index = insn.getOpcode() - ResolveOpcodes.LESS;
            int opcode = opcodes[index];
            org.objectweb.asm.Label lbl = new org.objectweb.asm.Label();
            org.objectweb.asm.Label lbl1 = new org.objectweb.asm.Label();
            writer.visitLabel(new org.objectweb.asm.Label());
            writer.visitJumpInsn(opcode, lbl);
            writer.visitInsn(ICONST_0);
            writer.visitJumpInsn(GOTO, lbl1);
            writer.visitLabel(lbl);
            writer.visitInsn(ICONST_1);
            writer.visitLabel(lbl1);
        } else if(insn instanceof OperationInsn) {
            OperationInsn multInsn = (OperationInsn)insn;
            int startingOpcode = startingOpcodes.getOrDefault(multInsn.getOpcode(), -101);
            Type primitiveType = getPrimitiveType(multInsn.getResultType());
            if(primitiveType != null) {
                writer.visitInsn(primitiveType.getOpcode(startingOpcode));
            } else {
                //writer.visitVarInsn(ALOAD, multInsn.getVarIndex());
            }
        } else if(insn instanceof LoadNullInsn) {
            writer.visitInsn(ACONST_NULL);
        } else if(insn instanceof PopInsn) {
            WeacType removed = ((PopInsn)insn).getRemovedType();
            Type primitive = getPrimitiveType(removed);
            if(primitive == Type.LONG_TYPE || primitive == Type.DOUBLE_TYPE) {
                writer.visitInsn(POP2);
            } else {
                writer.visitInsn(POP);
            }
        } else if(insn instanceof FunctionCallInsn) {
            FunctionCallInsn callInsn = (FunctionCallInsn)insn;
            Type[] argTypes = new Type[callInsn.getArgCount()];
            for(int i0 = 0;i0<callInsn.getArgCount();i0++) {
                argTypes[i0] = toJVMType(callInsn.getArgTypes()[i0]);
            }
            int invokeType = INVOKEVIRTUAL;
            if(callInsn.isStatic()) {
                invokeType = INVOKESTATIC;
            } else if(callInsn.getName().equals("<init>")) {
                invokeType = INVOKESPECIAL;
            }
            WeacType owner = callInsn.getOwner();
            if(owner.getSuperType() != null) {
                if(owner.getSuperType().equals(JVMWeacTypes.PRIMITIVE_TYPE)) {
                    String methodDesc = Type.getMethodDescriptor(invokeType == INVOKESPECIAL ? Type.VOID_TYPE : toJVMType(callInsn.getReturnType()), toJVMType(owner, false));
                    writer.visitMethodInsn(INVOKESTATIC, toJVMType(owner, true, false).getInternalName(), callInsn.getName(), methodDesc, false);
                } else {
                    String methodDesc = Type.getMethodDescriptor(invokeType == INVOKESPECIAL ? Type.VOID_TYPE : toJVMType(callInsn.getReturnType()), argTypes);
                    writer.visitMethodInsn(invokeType, toJVMType(owner, true).getInternalName(), callInsn.getName(), methodDesc, false);
                }
            } else {
                String methodDesc = Type.getMethodDescriptor(invokeType == INVOKESPECIAL ? Type.VOID_TYPE : toJVMType(callInsn.getReturnType()), argTypes);
                writer.visitMethodInsn(invokeType, toJVMType(owner, true).getInternalName(), callInsn.getName(), methodDesc, false);
            }
        } else if(insn.getOpcode() == ResolveOpcodes.DUP) {
            writer.visitInsn(DUP);
        } else if(insn.getOpcode() == ResolveOpcodes.THROW) {
            writer.visitInsn(ATHROW);
        } else if(insn instanceof NewInsn) {
            writer.visitTypeInsn(NEW, toJVMType(((NewInsn) insn).getType()).getInternalName());
        } else if(insn instanceof GotoResInsn) {
            org.objectweb.asm.Label lbl = labelMap.get(((GotoResInsn) insn).getDestination());
            writer.visitJumpInsn(GOTO, lbl);
        } else if(insn instanceof IfNotJumpResInsn) {
            org.objectweb.asm.Label lbl = labelMap.get(((IfNotJumpResInsn) insn).getDestination());
            writer.visitLabel(new org.objectweb.asm.Label());
            writer.visitJumpInsn(IFEQ, lbl);
        } else if(insn instanceof ObjectEqualInsn) {
            org.objectweb.asm.Label lbl = new org.objectweb.asm.Label();
            org.objectweb.asm.Label lbl1 = new org.objectweb.asm.Label();
            writer.visitLabel(new org.objectweb.asm.Label());
            writer.visitJumpInsn(IF_ACMPNE, lbl);
            writer.visitInsn(ICONST_1);
            writer.visitJumpInsn(GOTO, lbl1);
            writer.visitLabel(lbl);
            writer.visitInsn(ICONST_0);
            writer.visitLabel(lbl1);
        } else if(insn instanceof CheckZero) {
            org.objectweb.asm.Label lbl = new org.objectweb.asm.Label();
            org.objectweb.asm.Label lbl1 = new org.objectweb.asm.Label();
            writer.visitLabel(new org.objectweb.asm.Label());
            writer.visitJumpInsn(IFNE, lbl);
            writer.visitInsn(ICONST_1);
            writer.visitJumpInsn(GOTO, lbl1);
            writer.visitLabel(lbl);
            writer.visitInsn(ICONST_0);
            writer.visitLabel(lbl1);
        } else if(insn instanceof LocalVariableTableInsn) {
            List<VariableValue> locals = ((LocalVariableTableInsn) insn).getLocals();
            for(VariableValue local : locals) {
                if(!local.getName().endsWith("this")) {
                    if(!registredLocals.contains(local.getName())) {
                        registredLocals.add(local.getName());
                        writer.visitLocalVariable(local.getName(), toJVMType(local.getType()).getDescriptor(), null,
                            /*start, end*/new org.objectweb.asm.Label(), new org.objectweb.asm.Label(), local.getLocalVariableIndex()+varIndexOffset);
                    }
                }
            }
        } else if(insn instanceof CastInsn) {
            CastInsn cInsn = ((CastInsn) insn);
            WeacType from = cInsn.getFrom();
            WeacType to = cInsn.getTo();
            if(from.isPrimitive() && to.isPrimitive()) {
                handlePrimitiveCast(from, to, writer);
            } else if(!from.isPrimitive() && !to.isPrimitive()) {
                writer.visitTypeInsn(CHECKCAST, toJVMType(to).getInternalName());
            } else {
                System.out.println("HALP: "+from+" -> "+to);
            }
        } else if(insn instanceof MaxsInsn) {
            MaxsInsn maxInsn = ((MaxsInsn) insn);
            int maxStack = maxInsn.getMaxStack();
            int maxLocal = maxInsn.getMaxLocal();
            writer.visitMaxs(maxStack, maxLocal);
        } else if(insn instanceof IfNotNullJumpInsn) {
            org.objectweb.asm.Label lbl = labelMap.get(((IfNotNullJumpInsn) insn).getDestination());
            writer.visitLabel(new org.objectweb.asm.Label());
            writer.visitJumpInsn(IFNONNULL, lbl);
        } else if(insn instanceof IfNullJumpInsn) {
            org.objectweb.asm.Label lbl = labelMap.get(((IfNullJumpInsn) insn).getDestination());
            writer.visitLabel(new org.objectweb.asm.Label());
            writer.visitJumpInsn(IFNULL, lbl);
        } else if(insn instanceof NativeCodeInstruction) {
            BytecodeSequencesInsn bytecodeSequences = (BytecodeSequencesInsn)insn;
            bytecodeSequences.getSequences().forEach(s -> s.write(writer, varIndexOffset));
        } else if(insn instanceof LineNumberInstruction) {
            LineNumberInstruction lineNumberInstruction = (LineNumberInstruction)insn;
            writer.visitLineNumber(lineNumberInstruction.getLineNumber(), currentLabel);
        } else if(insn instanceof LoadClassInsn) {
            LoadClassInsn loadClassInsn = (LoadClassInsn)insn;
            writer.visitLdcInsn(toJVMType(loadClassInsn.getClassToLoad()));
        } else if(insn instanceof FunctionStartResInsn) {
            // discard
        } else {
            System.err.println("unknown: "+insn);
        }
    }

    private void handlePrimitiveCast(WeacType from, WeacType to, MethodVisitor writer) {
        primitiveCastCompiler.compile(getPrimitiveType(from).getDescriptor(), getPrimitiveType(to).getDescriptor(), writer);
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

        mv.visitMaxs(1+(isStatic ? 0 : 1),0);
        mv.visitEnd();
    }

    private void writeStaticBlock(ClassWriter writer, Type type, ResolvedClass clazz) {
        MethodVisitor mv = writer.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        mv.visitLabel(new org.objectweb.asm.Label());
        if(clazz.classType == EnumClassTypes.OBJECT) {
            mv.visitTypeInsn(NEW, type.getInternalName());
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, type.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
            // store it in the field
            mv.visitFieldInsn(PUTSTATIC, type.getInternalName(), Constants.SINGLETON_INSTANCE_FIELD, type.getDescriptor());
        } else if(clazz.classType == EnumClassTypes.ENUM) {
            ArrayList<String> locals = new ArrayList<>();
            clazz.enumConstants.forEach(cst -> locals.add(cst.name));
            clazz.enumConstants.forEach(cst -> {
                mv.visitTypeInsn(NEW, type.getInternalName());
                mv.visitInsn(DUP);
                cst.parameters.forEach(instructions -> {
                    mv.visitLdcInsn(cst.name);
                    mv.visitLdcInsn(cst.ordinal);
                    compileSingleExpression(type, mv, instructions, new org.objectweb.asm.Label(), new org.objectweb.asm.Label(), 2, locals);
                });
                ConstructorInfos cons = cst.usedConstructor;
                Type[] arguments = new Type[cons.argTypes.size()+2];
                for (int i = 0; i < arguments.length-2; i++) {
                    arguments[i+2] = toJVMType(cons.argTypes.get(i));
                }
                arguments[0] = Type.getType(String.class);
                arguments[1] = Type.INT_TYPE;
                mv.visitMethodInsn(INVOKESPECIAL, type.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, arguments), false);
                mv.visitFieldInsn(PUTSTATIC, type.getInternalName(), cst.name, type.getDescriptor());
            });

            int size = clazz.enumConstants.size();
            mv.visitLdcInsn(size);
            mv.visitTypeInsn(ANEWARRAY, type.getInternalName());
            for (int i = 0; i < size; i++) {
                mv.visitInsn(DUP);
                mv.visitLdcInsn(i);
                mv.visitFieldInsn(GETSTATIC, type.getInternalName(), clazz.enumConstants.get(i).name, type.getDescriptor());
                mv.visitInsn(AASTORE);
            }
            mv.visitFieldInsn(PUTSTATIC, type.getInternalName(), "$VALUES", "["+type.getDescriptor());
        }
        mv.visitLabel(new org.objectweb.asm.Label());
        mv.visitInsn(RETURN);
        mv.visitMaxs(2,0);
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

    public static Type toJVMType(WeacType type) {
        return toJVMType(type, false);
    }

    public static Type toJVMType(WeacType type, boolean isFunctionOwner) {
        return toJVMType(type, isFunctionOwner, true);
    }

    public static Type toJVMType(WeacType type, boolean isFunctionOwner, boolean convertPrimitives) {
        if(!isFunctionOwner) {
            Type primitiveType = getPrimitiveType(type);
            if(primitiveType != null)
                return primitiveType;
        }
        return Type.getType(toDescriptor(type, convertPrimitives));
    }

    private String toDescriptor(WeacType type) {
        return toDescriptor(type, true);
    }

    public static String toDescriptor(WeacType type, boolean convertPrimitives) {
        Type primitiveType = getPrimitiveType(type);
        if(primitiveType != null && convertPrimitives)
            return primitiveType.getDescriptor();
        if(type.equals(JVMWeacTypes.VOID_TYPE)) {
            return "V";
        }
        if(!type.isValid()) {
            System.err.println("INV: "+type.getIdentifier());
            return "I";
        }
        StringBuilder builder = new StringBuilder();
        if(type.isArray()) {
            builder.append("[");
            builder.append(toDescriptor(type.getArrayType(), convertPrimitives));
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
                type = ACC_ANNOTATION | ACC_INTERFACE;
                abstractness = ACC_ABSTRACT;
                break;

            case INTERFACE:
                type = ACC_INTERFACE;
                abstractness = ACC_ABSTRACT;
                break;

            case ENUM:
                type = ACC_ENUM;
                break;

            case OBJECT:
            case DATA:
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
            abstractness = ACC_ABSTRACT;
        }

        return access | type | abstractness;
    }

}
