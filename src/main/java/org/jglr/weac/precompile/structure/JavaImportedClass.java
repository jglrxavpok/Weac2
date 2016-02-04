package org.jglr.weac.precompile.structure;

import org.jglr.weac.parse.EnumClassTypes;
import org.jglr.weac.utils.Identifier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class JavaImportedClass extends WeacPrecompiledClass {

    public JavaImportedClass(Class<?> clazz) {
        super();
        if(clazz.isInterface()) {
            classType = EnumClassTypes.INTERFACE;
        } else if(clazz.isEnum()) {
            classType = EnumClassTypes.ENUM;
        } else {
            classType = EnumClassTypes.CLASS;
        }
        name = clazz.getSimpleName();
        packageName = clazz.getPackage().getName();
        fullName = clazz.getCanonicalName();
        Method[] jmethods = clazz.getDeclaredMethods();
        for(Method m : jmethods) {
            WeacPrecompiledMethod convertedMethod = new WeacPrecompiledMethod();
            convertedMethod.name = new Identifier(m.getName());
            String classType = m.getReturnType().getCanonicalName();
            convertedMethod.returnType = new Identifier(classType, true);

            // TODO: args
            methods.add(convertedMethod);
        }

        Field[] jfields = clazz.getDeclaredFields();
        for(Field f : jfields) {
            WeacPrecompiledField convertedField = new WeacPrecompiledField();
            convertedField.name = new Identifier(f.getName());
            String classType = f.getType().getCanonicalName();
            convertedField.type = new Identifier(classType, true);
            // TODO: default value
            fields.add(convertedField);
        }
    }
}
