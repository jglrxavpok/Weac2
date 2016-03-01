package weac.compiler.precompile.structure;

import weac.compiler.parse.EnumClassTypes;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.WeacType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class JavaImportedClass extends PrecompiledClass {

    private JavaImportedClass superclass;

    public JavaImportedClass(Class<?> clazz) {
        super();
        if(clazz.isInterface()) {
            classType = EnumClassTypes.INTERFACE;
        } else if(clazz.isEnum()) {
            classType = EnumClassTypes.ENUM;
        } else {
            classType = EnumClassTypes.CLASS;
        }
        packageName = clazz.getPackage().getName();
        fullName = clazz.getCanonicalName();
        Method[] jmethods = clazz.getDeclaredMethods();
        for(Method m : jmethods) {
            PrecompiledMethod convertedMethod = new PrecompiledMethod();
            convertedMethod.name = new Identifier(m.getName());
            WeacType returnType = toWeacType(m.getReturnType().getCanonicalName());
            if(returnType == null) {
                returnType = new WeacType(null, m.getReturnType().getCanonicalName(), true);
            }
            convertedMethod.returnType = new Identifier(returnType.getIdentifier().getId(), true);

            Parameter[] parameters = m.getParameters();
            for(Parameter p : parameters) {
                convertedMethod.argumentNames.add(new Identifier(p.getName(), true));
                WeacType argType = toWeacType(p.getType().getCanonicalName());
                if(argType == null) {
                    argType = new WeacType(null, p.getType().getCanonicalName(), true);
                }
                convertedMethod.argumentTypes.add(argType.getIdentifier());
            }
            convertedMethod.isJavaImported = true;

//            System.out.println("Imported method: "+convertedMethod.name+" / "+convertedMethod.returnType+" / "+ Arrays.toString(convertedMethod.argumentTypes.toArray()));
            methods.add(convertedMethod);
        }

        Field[] jfields = clazz.getDeclaredFields();
        for(Field f : jfields) {
            PrecompiledField convertedField = new PrecompiledField();
            convertedField.name = new Identifier(f.getName());
            String classType = f.getType().getCanonicalName();
            convertedField.type = new Identifier(classType, true);
            // TODO: default value
            fields.add(convertedField);
        }

        if(clazz.getSuperclass() != null)
            superclass = new JavaImportedClass(clazz.getSuperclass());
        if(superclass != null)
            motherClass = superclass.fullName;

        name = new WeacType(superclass != null ? superclass.name : WeacType.OBJECT_TYPE, clazz.getSimpleName(), false);
    }

    public JavaImportedClass getSuperclass() {
        return superclass;
    }

    public static WeacType toWeacType(String id) {
        WeacType fieldType = null;
        switch (id) {
            case "boolean":
                fieldType = WeacType.BOOLEAN_TYPE;
                break;

            case "byte":
                fieldType = WeacType.BYTE_TYPE;
                break;

            case "char":
                fieldType = WeacType.CHAR_TYPE;
                break;

            case "double":
                fieldType = WeacType.DOUBLE_TYPE;
                break;

            case "float":
                fieldType = WeacType.FLOAT_TYPE;
                break;

            case "int":
                fieldType = WeacType.INTEGER_TYPE;
                break;

            case "long":
                fieldType = WeacType.LONG_TYPE;
                break;

            case "short":
                fieldType = WeacType.SHORT_TYPE;
                break;

            case "java.lang.String":
                fieldType = WeacType.STRING_TYPE;
                break;

            case "void":
                fieldType = WeacType.VOID_TYPE;
                break;
        }
        return fieldType;
    }
}
