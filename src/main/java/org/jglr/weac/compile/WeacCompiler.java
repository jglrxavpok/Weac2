package org.jglr.weac.compile;

import org.jglr.weac.WeacCompileUtils;
import org.jglr.weac.parse.EnumClassTypes;
import org.jglr.weac.precompile.structure.WeacPrecompiledClass;
import org.jglr.weac.resolve.structure.WeacResolvedClass;
import org.jglr.weac.resolve.structure.WeacResolvedSource;
import org.objectweb.asm.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeacCompiler extends WeacCompileUtils implements Opcodes {

    public Map<String, byte[]> process(WeacResolvedSource source) {
        Map<String, byte[]> compiledClasses = new HashMap<>();

        for(WeacResolvedClass clazz : source.classes) {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            String internalName = toInternal(clazz.fullName);
            Type type = Type.getType("L"+internalName+";");

            String signature = null;
            String superclass = Type.getInternalName(Object.class);
            if(clazz.classType != EnumClassTypes.INTERFACE) {
               superclass = toInternal(clazz.parents.getSuperclass());
            }
            writer.visit(V1_8, convertAccessToASM(clazz), internalName, signature, superclass, convertToASM(clazz.parents.getInterfaces()));

            if(clazz.classType == EnumClassTypes.OBJECT) {
                writer.visitField(ACC_PUBLIC + ACC_STATIC, "__instance__", type.getDescriptor(), null, null);

                writeGetter(writer, true, type, type, "__instance__");
            }

            writeStaticBlock(writer, type, clazz);
            writer.visitEnd();

            compiledClasses.put(clazz.fullName, writer.toByteArray());
        }
        return compiledClasses;
    }

    private void writeGetter(ClassWriter writer, boolean isStatic, Type owner, Type fieldType, String fieldName) {
        String getterName = "get"+Character.toUpperCase(fieldName.charAt(0))+fieldName.substring(1);
        MethodVisitor mv = writer.visitMethod(ACC_PUBLIC + (isStatic ? ACC_STATIC : 0), getterName, Type.getMethodDescriptor(fieldType), null, null);
        mv.visitCode();
        mv.visitLabel(new Label());
        if(!isStatic) {
            mv.visitVarInsn(ALOAD, 0); // load 'this'
        }
        mv.visitFieldInsn(isStatic ? GETSTATIC : GETFIELD, owner.getInternalName(), fieldName, fieldType.getDescriptor());
        mv.visitInsn(ARETURN);

        mv.visitMaxs(0,0);
        mv.visitEnd();
    }

    private void writeStaticBlock(ClassWriter writer, Type type, WeacResolvedClass clazz) {
        MethodVisitor mv = writer.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        if(clazz.classType == EnumClassTypes.OBJECT) {
            mv.visitLabel(new Label());
            mv.visitTypeInsn(NEW, type.getInternalName());
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, type.getInternalName(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
            // store it in the field
            mv.visitFieldInsn(PUTSTATIC, type.getInternalName(), "__instance__", type.getDescriptor());
        }
        mv.visitLabel(new Label());
        mv.visitInsn(RETURN);
        mv.visitMaxs(0,0);
        mv.visitEnd();
    }

    private String toInternal(WeacPrecompiledClass clazz) {
        if(clazz == null)
            return null;
        return toInternal(clazz.fullName);
    }

    private String[] convertToASM(List<WeacPrecompiledClass> interfaces) {
        String[] interfaceArray = new String[interfaces.size()];

        int index = 0;
        for(WeacPrecompiledClass clazz : interfaces) {
            interfaceArray[index++] = toInternal(clazz.fullName);
        }

        return interfaceArray;
    }

    private String toInternal(String fullName) {
        return fullName.replace(".", "/");
    }

    private int convertAccessToASM(WeacResolvedClass clazz) {
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
                type = ACC_ANNOTATION;
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

        return access | type | abstractness;
    }

}
