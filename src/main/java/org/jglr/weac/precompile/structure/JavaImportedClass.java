package org.jglr.weac.precompile.structure;

import org.jglr.weac.parse.EnumClassTypes;
import org.jglr.weac.utils.Identifier;

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
            methods.add(convertedMethod);
        }
    }
}
