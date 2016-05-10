package weac.compiler.targets.jvm.resolve;

import weac.compiler.precompile.structure.JavaImportedClass;
import weac.compiler.precompile.structure.PrecompiledClass;
import weac.compiler.resolve.ClassHierarchy;
import weac.compiler.resolve.Resolver;
import weac.compiler.resolve.ResolvingContext;
import weac.compiler.resolve.TypeResolver;
import weac.compiler.targets.jvm.JVMWeacTypes;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.Import;
import weac.compiler.utils.WeacType;

import java.util.List;

public class JVMTypeResolver extends TypeResolver {

    private final Resolver resolver;

    public JVMTypeResolver(Resolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public WeacType resolveType(Identifier type, ResolvingContext context) {
        if(type.equals(WeacType.AUTO.getIdentifier())) {
            return WeacType.AUTO;
        }
        WeacType primitiveType = getPotentialPrimitive(type);
        if(primitiveType != null) {
            return primitiveType;
        }

        WeacType intermediateType = new WeacType(null, type.getId(), true);
        String core = intermediateType.getCoreType().getIdentifier().getId();

        WeacType primitive = getPotentialPrimitive(new Identifier(core));
        if(primitive != null) {
            return new WeacType(JVMWeacTypes.PRIMITIVE_TYPE, primitive.getIdentifier().getId()+(type.getId().substring(core.length())), true);
        }
        PrecompiledClass typeClass = resolver.findClass(core, context);
        if(typeClass == null) {
            // TODO: newError("Invalid type: "+type.getId()+" in "+context.getSource().classes.get(0).fullName, -1);
        }

        WeacType superclass = null;
        if (typeClass != null && !typeClass.fullName.equals(JVMWeacTypes.OBJECT_TYPE.getIdentifier().toString()) // WeacObject
                && !typeClass.fullName.equals(JVMWeacTypes.JOBJECT_TYPE.getIdentifier().toString())) { // Java's Object
            String superclassName = typeClass.motherClass;
            if(superclassName != null) {
                PrecompiledClass superTypeClass = resolver.findClass(superclassName, context);
                if (superTypeClass != null && superTypeClass.name != null) {
                    superclass = resolveType(new Identifier(superTypeClass.fullName, true), context);
                }
            }
        }
        return new WeacType(superclass, typeClass.fullName+(type.getId().substring(core.length())), true);
    }

    public WeacType findResultType(WeacType left, WeacType right, ResolvingContext context) {
        if(left.equals(right)) {
            return left;
        } else if(resolver.isCastable(left, right, context)) {
            return right;
        } else if(resolver.isCastable(right, left, context)) {
            return left;
        }
        return JVMWeacTypes.JOBJECT_TYPE;
    }

    @Override
    public boolean isCastable(PrecompiledClass from, PrecompiledClass to, ResolvingContext context) {
        if(from.fullName.equals("java.lang.Object") && !to.fullName.equals("java.lang.Object"))
            return false;
        ClassHierarchy hierarchy = resolver.getHierarchy(from, from.interfacesImplemented, context);

        // check superclass
        if(hierarchy.getSuperclass().equals(to)) {
            return true;
        }

        // check interfaces
        for(PrecompiledClass inter : hierarchy.getInterfaces()) {
            if(inter.equals(to)) {
                return true;
            }
        }

        if(hierarchy.getSuperclass().equals(from)) {
            return false; // we have reached the top of the hierarchy!
        }

        // check superclass hierarchy
        return isCastable(hierarchy.getSuperclass(), to, context);
    }

    @Override
    public PrecompiledClass findClass(String inter, ResolvingContext context) {
        String transformedName = transform(inter, context.getSource().imports);
        //System.out.println(inter+" transformed to "+transformedName+" with "+context.getSource().classes.get(0).fullName);
        // check in source if can be found
        for (PrecompiledClass sourceClass : context.getSource().classes) {
            if(sourceClass.fullName.equals(transformedName)) {
                return sourceClass;
            }

            if(sourceClass.name.getCoreType().toString().equals(inter)) {
                return sourceClass;
            }
        }

        for (PrecompiledClass sideClass : context.getSideClasses()) {
            if(sideClass.fullName.equals(transformedName)) {
                return sideClass;
            }
        }

        // test for base classes, because they are not necessarily imported
        for (PrecompiledClass sideClass : context.getSideClasses()) {
            if(sideClass.fullName.equals("weac.lang."+inter)) {
                return sideClass;
            }

            if(sideClass.fullName.equals("java.lang."+inter)) {
                return sideClass;
            }
        }

        Class<?> javaClass;
        // try importing from java
        try {
            javaClass = Class.forName(transformedName, false, getClass().getClassLoader());
            if(javaClass != null) {
                return new JavaImportedClass(javaClass);
            }
        } catch (Exception e) {

            // try importing from java.lang
            try {
                javaClass = Class.forName("java.lang."+inter, false, getClass().getClassLoader());
                if(javaClass != null) {
                    return new JavaImportedClass(javaClass);
                }
            } catch (ClassNotFoundException e1) {
                e.printStackTrace();
                throw new RuntimeException(inter+"("+transformedName+")"+" / "+context.getSource().classes.get(0).fullName, e1);
            }
        }

        // TODO: better logging
        System.err.println("NOT FOUND: \""+inter+"\""+" / \""+transformedName+"\"");

        return null;
    }

    private String transform(String name, List<Import> imports) {
        for(Import imp : imports) {
            if(imp.usageName != null) {
                if (imp.usageName.equals(name)) {
                    return imp.importedType;
                }
            } else if(imp.importedType.endsWith("."+name)) {
                return imp.importedType;
            }
        }
        return name;
    }

    private WeacType getPotentialPrimitive(Identifier type) {
        switch (type.getId()) {
            case "void":
                return JVMWeacTypes.VOID_TYPE;

            case "boolean":
                return JVMWeacTypes.BOOLEAN_TYPE;

            case "byte":
                return JVMWeacTypes.BYTE_TYPE;

            case "char":
                return JVMWeacTypes.CHAR_TYPE;

            case "double":
                return JVMWeacTypes.DOUBLE_TYPE;

            case "float":
                return JVMWeacTypes.FLOAT_TYPE;

            case "int":
                return JVMWeacTypes.INTEGER_TYPE;

            case "long":
                return JVMWeacTypes.LONG_TYPE;

            case "short":
                return JVMWeacTypes.SHORT_TYPE;
        }
        return null;
    }
}
