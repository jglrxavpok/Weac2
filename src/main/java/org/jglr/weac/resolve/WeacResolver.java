package org.jglr.weac.resolve;

import org.jglr.weac.WeacCompileUtils;
import org.jglr.weac.patterns.WeacInstructionPattern;
import org.jglr.weac.precompile.WeacPreCompiler;
import org.jglr.weac.precompile.insn.*;
import org.jglr.weac.precompile.structure.*;
import org.jglr.weac.resolve.insn.*;
import org.jglr.weac.resolve.structure.*;
import org.jglr.weac.utils.Identifier;
import org.jglr.weac.utils.WeacImport;
import org.jglr.weac.utils.WeacType;

import java.math.BigDecimal;
import java.util.*;

public class WeacResolver extends WeacCompileUtils {

    private final List<WeacInstructionPattern<WeacResolvedInsn>> patterns;
    private final NumberResolver numberResolver;

    public WeacResolver() {
        patterns = new LinkedList<>();
        numberResolver = new NumberResolver();
    }

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
        resolvedClass.annotations.addAll(resolveAnnotations(aClass.annotations, source, sideClasses));
        resolvedClass.classType = aClass.classType;
        resolvedClass.parents = getInterfaces(aClass, aClass.interfacesImplemented, source, sideClasses);
        resolvedClass.fullName = getFullName(aClass);
        resolvedClass.enumConstants = resolveEnums(aClass.enumConstants);
        resolvedClass.isAbstract = aClass.isAbstract;
        resolvedClass.isMixin = aClass.isMixin;
        resolvedClass.name = aClass.name;
        resolvedClass.isCompilerSpecial = aClass.isCompilerSpecial;

        WeacMixedContentClass toMixIn = resolveMixins(resolvedClass, resolvedClass.parents.getMixins());
        resolvedClass.methods = resolveMethods(resolvedClass, aClass, source, sideClasses, toMixIn);

        resolvedClass.fields = resolveFields(resolvedClass, aClass, source, sideClasses, toMixIn);
        return resolvedClass;
    }

    private List<WeacResolvedAnnotation> resolveAnnotations(List<WeacPrecompiledAnnotation> annotations, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        List<WeacResolvedAnnotation> resolvedAnnotations = new LinkedList<>();
        for(WeacPrecompiledAnnotation a : annotations) {
            WeacPrecompiledSource annotSource = new WeacPrecompiledSource();
            WeacPrecompiledClass clazz = findClass(a.getName(), source, sideClasses);
            annotSource.classes = new LinkedList<>();
            annotSource.classes.add(clazz);
            annotSource.classes.addAll(source.classes); // corner cases where the annotation uses the type it is describing

            annotSource.imports = new LinkedList<>();
            if(clazz == null)
                throw new RuntimeException(":cc "+a.getName());
            annotSource.packageName = clazz.packageName;
            WeacResolvedAnnotation resolved = new WeacResolvedAnnotation(resolve(annotSource, clazz, sideClasses));
            a.args.stream()
                    .map(this::resolveSingleExpression)
                    .forEach(resolved.getArgs()::add);
            resolvedAnnotations.add(resolved);
        }
        return resolvedAnnotations;
    }

    private List<WeacResolvedField> resolveFields(WeacResolvedClass resolvedClass, WeacPrecompiledClass aClass, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses, WeacMixedContentClass toMixIn) {
        if(resolvedClass.isMixin) {
            return Collections.emptyList();
        }
        List<WeacResolvedField> fields = new LinkedList<>();
        toMixIn.fields.forEach(m -> addOrOverrideField(resolveSingleField(m, source, sideClasses), fields));

        aClass.fields.forEach(m -> addOrOverrideField(resolveSingleField(m, source, sideClasses), fields));
        return fields;
    }

    private WeacResolvedField resolveSingleField(WeacPrecompiledField field, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        WeacResolvedField resolvedField = new WeacResolvedField();
        resolvedField.name = field.name;
        resolvedField.access = field.access;
        resolvedField.isCompilerSpecial = field.isCompilerSpecial;
        resolvedField.type = resolveType(field.type, source, sideClasses);
        resolvedField.defaultValue.addAll(resolveSingleExpression(field.defaultValue));
        return resolvedField;
    }

    private List<WeacResolvedMethod> resolveMethods(WeacResolvedClass resolvedClass, WeacPrecompiledClass aClass, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses, WeacMixedContentClass toMixIn) {
        List<WeacResolvedMethod> methods = new LinkedList<>();
        toMixIn.methods.forEach(m -> addOrOverride(resolveSingleMethod(m, source, sideClasses), methods));

        aClass.methods.forEach(m -> addOrOverride(resolveSingleMethod(m, source, sideClasses), methods));
        return methods;
    }

    private WeacResolvedMethod resolveSingleMethod(WeacPrecompiledMethod precompiledMethod, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        WeacResolvedMethod method = new WeacResolvedMethod();
        method.access = precompiledMethod.access;
        method.annotations.addAll(resolveAnnotations(precompiledMethod.annotations, source, sideClasses));
        method.argumentNames.addAll(precompiledMethod.argumentNames);
        method.isAbstract = precompiledMethod.isAbstract;
        method.isConstructor = precompiledMethod.isConstructor;
        method.name = precompiledMethod.name;
        method.isCompilerSpecial = precompiledMethod.isCompilerSpecial;
        method.returnType = resolveType(precompiledMethod.returnType, source, sideClasses);
        resolveSingleExpression(precompiledMethod.instructions).forEach(method.instructions::add);
        precompiledMethod.argumentTypes.stream()
                .map(t -> resolveType(t, source, sideClasses))
                .forEach(method.argumentTypes::add);
        return method;
    }

    private WeacType resolveType(Identifier type, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        if(type.getId().equalsIgnoreCase("Void"))
            return WeacType.VOID_TYPE;
        WeacType intermediateType = new WeacType(type.getId(), true);
        String core = intermediateType.getCoreType().getIdentifier().getId();
        WeacPrecompiledClass typeClass = findClass(core, source, sideClasses);
        if(typeClass == null) {
            newError("Invalid type: "+type.getId()+" in "+source.classes.get(0).fullName, -1);
        }
        return new WeacType(typeClass.fullName+(type.getId().substring(core.length())), true);
    }

    private void addOrOverrideField(WeacResolvedField toAdd, List<WeacResolvedField> fieldList) {
        Iterator<WeacResolvedField> iterator = fieldList.iterator();
        while (iterator.hasNext()) {
            WeacResolvedField existing = iterator.next();
            if(existing.name.equals(toAdd.name)) { // same name
                iterator.remove();
            }
        }
        fieldList.add(toAdd);
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
                        parents.addInterface(clazz);
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

            if(sourceClass.name.equals(inter)) {
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

        // try importing from java.lang
        try {
            Class<?> javaClass = Class.forName("java.lang."+inter, false, getClass().getClassLoader());
            if(javaClass != null) {
                return new JavaImportedClass(javaClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.err.println("NOT FOUND: \""+inter+"\"");

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
            WeacResolvedEnumConstant resolved = new WeacResolvedEnumConstant();
            resolved.name = cst.name;

            cst.parameters.stream()
                    .map(this::resolveSingleExpression)
                    .forEach(resolved.parameters::add);

            resolvedConstants.add(resolved);
        }
        return resolvedConstants;
    }

    private List<WeacResolvedInsn> resolveSingleExpression(List<WeacPrecompiledInsn> precompiled) {
        List<WeacResolvedInsn> insns = new LinkedList<>();
        for(int i = 0;i<precompiled.size();i++) {
            WeacPrecompiledInsn precompiledInsn = precompiled.get(i);
            if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_NUMBER_CONSTANT) {
                WeacLoadNumberConstant cst = ((WeacLoadNumberConstant) precompiledInsn);
                String numberRepresentation = cst.getValue();
                insns.add(numberResolver.resolve(numberRepresentation));
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_BOOLEAN_CONSTANT) {
                WeacLoadBooleanConstant cst = ((WeacLoadBooleanConstant) precompiledInsn);
                insns.add(new WeacLoadBooleanInsn(cst.getValue()));
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_STRING_CONSTANT) {
                WeacLoadStringConstant cst = ((WeacLoadStringConstant) precompiledInsn);
                insns.add(new WeacLoadStringInsn(cst.getValue()));
            }
        }
        return insns;
    }

}
