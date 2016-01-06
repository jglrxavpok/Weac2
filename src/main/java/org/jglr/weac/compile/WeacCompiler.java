package org.jglr.weac.compile;

import org.jglr.weac.WeacCompilePhase;
import org.jglr.weac.WeacCompileUtils;
import org.jglr.weac.resolve.structure.WeacResolvedClass;
import org.jglr.weac.resolve.structure.WeacResolvedSource;
import org.jglr.weac.utils.WeacModifierType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeacCompiler extends WeacCompileUtils implements Opcodes {

    public Map<String, byte[]> process(WeacResolvedSource source) {
        Map<String, byte[]> compiledClasses = new HashMap<>();

        for(WeacResolvedClass clazz : source.classes) {
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            String internalName = toInternal(clazz.fullName);

            String signature = null;
            writer.visit(V1_8, convertAccessToASM(clazz), internalName, signature, clazz.motherClass.fullName, convertToASM(clazz.interfacesImplemented));
            writer.visitEnd();

            compiledClasses.put(clazz.fullName, writer.toByteArray());
        }
        return compiledClasses;
    }

    private String[] convertToASM(List<WeacResolvedClass> interfaces) {
        String[] interfaceArray = new String[interfaces.size()];

        int index = 0;
        for(WeacResolvedClass clazz : interfaces) {
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
