package org.jglr.weac.resolve;

import org.jglr.weac.WeacCompileUtils;
import org.jglr.weac.precompile.structure.WeacPrecompiledClass;
import org.jglr.weac.precompile.structure.WeacPrecompiledEnumConstant;
import org.jglr.weac.precompile.structure.WeacPrecompiledMethod;
import org.jglr.weac.precompile.structure.WeacPrecompiledSource;
import org.jglr.weac.resolve.structure.*;
import org.jglr.weac.utils.WeacImport;
import org.jglr.weac.utils.WeacType;

import java.util.*;

public class WeacResolver extends WeacCompileUtils {

    public WeacResolvedSource process(WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        WeacResolvedSource resolved = new WeacResolvedSource();
        resolved.packageName = source.packageName;
        resolved.classes = new ArrayList<>();

        source.classes.forEach(s -> resolved.classes.add(resolve(source, s, sideClasses)));
        return resolved;
    }

    private WeacResolvedClass resolve(WeacPrecompiledSource source, WeacPrecompiledClass aClass, WeacPrecompiledClass[] sideClasses) {
        WeacResolvedClass resolvedClass = new WeacResolvedClass();
        resolvedClass.access = aClass.access;
        resolvedClass.annotations = aClass.annotations;
        resolvedClass.classType = aClass.classType;
        resolvedClass.parents = getInterfaces(aClass, aClass.interfacesImplemented, source, sideClasses);
        resolvedClass.fullName = getFullName(aClass);
        resolvedClass.enumConstants = resolveEnums(aClass.enumConstants);
        resolvedClass.isAbstract = aClass.isAbstract;
        resolvedClass.isMixin = aClass.isMixin;
        resolvedClass.name = aClass.name;

        WeacMixedContentClass toMixIn = resolveMixins(resolvedClass, resolvedClass.parents.getMixins());
        resolvedClass.methods = resolveMethods(resolvedClass, aClass, source, sideClasses, toMixIn);
        return resolvedClass;
    }

    private List<WeacResolvedMethod> resolveMethods(WeacResolvedClass resolvedClass, WeacPrecompiledClass aClass, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses, WeacMixedContentClass toMixIn) {
        List<WeacResolvedMethod> methods = new LinkedList<>();
        toMixIn.methods.forEach(m -> addOrOverride(resolveSingleMethod(m), methods));

        aClass.methods.forEach(m -> addOrOverride(resolveSingleMethod(m), methods));
        return methods;
    }

    private WeacResolvedMethod resolveSingleMethod(WeacPrecompiledMethod precompiledMethod) {
        WeacResolvedMethod method = new WeacResolvedMethod();
        method.access = precompiledMethod.access;
        method.annotations.addAll(precompiledMethod.annotations);
        method.argumentNames.addAll(precompiledMethod.argumentNames);
        method.isAbstract = precompiledMethod.isAbstract;
        method.isConstructor = precompiledMethod.isConstructor;
        method.name = precompiledMethod.name;
        method.returnType = new WeacType(precompiledMethod.returnType);
        precompiledMethod.argumentTypes.stream().map(WeacType::new).forEach(method.argumentTypes::add);
        method.name = precompiledMethod.name;
        return method;
    }

    private void addOrOverride(WeacResolvedMethod toAdd, List<WeacResolvedMethod> methodList) {
        Iterator<WeacResolvedMethod> iterator = methodList.iterator();
        while (iterator.hasNext()) {
            WeacResolvedMethod existing = iterator.next();
            if(existing.name.equals(toAdd.name)) { // same name
                if(existing.argumentTypes.size() == toAdd.argumentTypes.size()) { // same count of arguments
                    for(int i = 0;i<existing.argumentTypes.size();i++) { // check if same argument types
                        WeacType toAddArgType = toAdd.argumentTypes.get(i);
                        WeacType existingArgType = existing.argumentTypes.get(i);
                        if(!toAddArgType.equals(existingArgType)) {
                            break; // not matching types, abord
                        }
                    }
                    iterator.remove();
                }
            }
        }
        methodList.add(toAdd);
    }

    private WeacMixedContentClass resolveMixins(WeacResolvedClass resolvedClass, List<WeacPrecompiledClass> mixins) {

        WeacMixedContentClass contentClass = new WeacMixedContentClass();

        for(WeacPrecompiledClass mixin : mixins) {
            // if some methods have the same name, last one should win
            contentClass.methods.addAll(mixin.methods);
            contentClass.fields.addAll(mixin.fields);
        }
        return contentClass;
    }

    private String getFullName(WeacPrecompiledClass aClass) {
        return (aClass.packageName == null || aClass.packageName.isEmpty()) ? aClass.name : aClass.packageName+"."+aClass.name;
    }

    private ClassParents getInterfaces(WeacPrecompiledClass aClass, List<String> interfacesImplemented, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        ClassParents parents = new ClassParents();

        List<String> interfaces = new ArrayList<>();
        interfaces.addAll(interfacesImplemented);

        if(!interfaces.contains(aClass.motherClass) && aClass.motherClass != null)
            interfaces.add(aClass.motherClass);

        WeacPrecompiledClass superclass = null;
        for(String inter : interfaces) {
            WeacPrecompiledClass clazz = findClass(inter, source, sideClasses);
            if(clazz == null) {
                newError("Class not found: "+inter, -1);
                return parents;
            }
            switch(clazz.classType) {
                case ANNOTATION:
                    // TODO
                    break;

                case ENUM:
                    newError("Cannot extend an enum", -1);
                    break;

                case OBJECT:
                    newError("Cannot extend an object", -1);
                    break;

                case STRUCT:
                    newError("Cannot extend a struct", -1);
                    break;

                case INTERFACE:
                    parents.addInterface(clazz);
                    break;

                case CLASS:
                    if(clazz.isMixin && !aClass.isMixin) {
                        parents.addMixin(clazz);
                    } else if(superclass == null && !aClass.isMixin) {
                        superclass = clazz;
                    } else if(!aClass.isMixin) {
                        newError("Cannot extend multiple classes that are not mixins.", -1);
                    } else {
                        newError("Cannot inherit classes to mixins.", -1);
                    }
                    break;
            }
        }
        if(superclass == null) {
            superclass = new WeacPrecompiledClass();
            superclass.name = "WeacObject";
            superclass.packageName = "weac.lang";
            superclass.fullName = "weac.lang.WeacObject";
        }
        parents.setSuperclass(superclass);
        return parents;
    }

    private WeacPrecompiledClass findClass(String inter, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        WeacPseudoResolvedClass clazz = new WeacPseudoResolvedClass();
        String transformedName = transform(inter, source.imports);
        System.out.println(inter+" transformed to "+transformedName);
        // check in source if can be found
        for (WeacPrecompiledClass sourceClass : source.classes) {
            if(sourceClass.fullName.equals(transformedName)) {
                return sourceClass;
            }
        }

        for (WeacPrecompiledClass sideClass : sideClasses) {
            if(sideClass.fullName.equals(transformedName)) {
                return sideClass;
            }
        }


        // test for base classes, because they are not necessarily imported
        for (WeacPrecompiledClass sideClass : sideClasses) {
            System.out.println(">>>>"+sideClass.fullName);
            if(sideClass.fullName.equals("weac.lang."+inter)) {
                return sideClass;
            }

            if(sideClass.fullName.equals("java.lang."+inter)) {
                return sideClass;
            }
        }

        return null;
    }

    private String transform(String name, List<WeacImport> imports) {
        for(WeacImport imp : imports) {
            if(imp.usageName != null) {
                if (imp.usageName.equals(name)) {
                    return imp.importedType;
                } else if(imp.importedType.endsWith("."+name)) {
                    return imp.importedType;
                }
            }
        }
        return name;
    }

    private List<WeacResolvedEnumConstant> resolveEnums(List<WeacPrecompiledEnumConstant> enumConstants) {
        List<WeacResolvedEnumConstant> resolvedConstants = new ArrayList<>();
        for(WeacPrecompiledEnumConstant cst : enumConstants) {

        }
        return resolvedConstants;
    }

}
