package org.jglr.weac.resolve;

import org.jglr.weac.WeacCompileUtils;
import org.jglr.weac.patterns.WeacInstructionPattern;
import org.jglr.weac.precompile.insn.*;
import org.jglr.weac.precompile.structure.*;
import org.jglr.weac.resolve.insn.*;
import org.jglr.weac.resolve.structure.*;
import org.jglr.weac.utils.Identifier;
import org.jglr.weac.utils.WeacImport;
import org.jglr.weac.utils.WeacType;

import java.util.*;

public class WeacResolver extends WeacCompileUtils {

    private final List<WeacInstructionPattern<WeacResolvedInsn>> patterns;
    private final NumberResolver numberResolver;
    private final StringResolver stringResolver;

    public WeacResolver() {
        patterns = new LinkedList<>();
        numberResolver = new NumberResolver();
        stringResolver = new StringResolver();
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
        WeacType currentType = resolveType(new Identifier(aClass.name), source, sideClasses);
        resolvedClass.access = aClass.access;
        resolvedClass.annotations.addAll(resolveAnnotations(aClass.annotations, currentType, source, sideClasses));
        resolvedClass.classType = aClass.classType;
        resolvedClass.parents = getInterfaces(aClass, aClass.interfacesImplemented, source, sideClasses);
        resolvedClass.fullName = getFullName(aClass);
        resolvedClass.enumConstants = resolveEnums(aClass.enumConstants, currentType, source, sideClasses);
        resolvedClass.isAbstract = aClass.isAbstract;
        resolvedClass.isMixin = aClass.isMixin;
        resolvedClass.name = aClass.name;
        resolvedClass.isCompilerSpecial = aClass.isCompilerSpecial;

        WeacMixedContentClass toMixIn = resolveMixins(resolvedClass, resolvedClass.parents.getMixins());
        resolvedClass.methods = resolveMethods(currentType, aClass, source, sideClasses, toMixIn);

        resolvedClass.fields = resolveFields(resolvedClass, currentType, aClass, source, sideClasses, toMixIn);
        return resolvedClass;
    }

    private List<WeacResolvedAnnotation> resolveAnnotations(List<WeacPrecompiledAnnotation> annotations, WeacType currentType, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
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
                    .map(l -> resolveSingleExpression(l, currentType, source, sideClasses, new VariableMap()))
                    .forEach(resolved.getArgs()::add);
            resolvedAnnotations.add(resolved);
        }
        return resolvedAnnotations;
    }

    private List<WeacResolvedField> resolveFields(WeacResolvedClass resolvedClass, WeacType currentType, WeacPrecompiledClass aClass, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses, WeacMixedContentClass toMixIn) {
        if(resolvedClass.isMixin) {
            return Collections.emptyList();
        }
        List<WeacResolvedField> fields = new LinkedList<>();
        VariableMap fieldVarMap = new VariableMap();
        toMixIn.fields.forEach(m -> addOrOverrideField(resolveSingleField(m, currentType, source, sideClasses, fieldVarMap), fields));

        aClass.fields.forEach(m -> addOrOverrideField(resolveSingleField(m, currentType, source, sideClasses, fieldVarMap), fields));
        return fields;
    }

    private WeacResolvedField resolveSingleField(WeacPrecompiledField field, WeacType currentType, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses, VariableMap fieldVarMap) {
        WeacResolvedField resolvedField = new WeacResolvedField();
        resolvedField.name = field.name;
        resolvedField.access = field.access;
        resolvedField.isCompilerSpecial = field.isCompilerSpecial;
        resolvedField.type = resolveType(field.type, source, sideClasses);
        resolvedField.defaultValue.addAll(resolveSingleExpression(field.defaultValue, currentType, source, sideClasses, fieldVarMap));
        return resolvedField;
    }

    private List<WeacResolvedMethod> resolveMethods(WeacType currentType, WeacPrecompiledClass aClass, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses, WeacMixedContentClass toMixIn) {
        List<WeacResolvedMethod> methods = new LinkedList<>();
        toMixIn.methods.forEach(m -> addOrOverride(resolveSingleMethod(m, currentType, source, sideClasses), methods));

        aClass.methods.forEach(m -> addOrOverride(resolveSingleMethod(m, currentType, source, sideClasses), methods));
        return methods;
    }

    private WeacResolvedMethod resolveSingleMethod(WeacPrecompiledMethod precompiledMethod, WeacType currentType, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        WeacResolvedMethod method = new WeacResolvedMethod();
        method.access = precompiledMethod.access;
        method.annotations.addAll(resolveAnnotations(precompiledMethod.annotations, currentType, source, sideClasses));
        method.argumentNames.addAll(precompiledMethod.argumentNames);
        method.isAbstract = precompiledMethod.isAbstract;
        method.isConstructor = precompiledMethod.isConstructor;
        method.name = precompiledMethod.name;
        method.isCompilerSpecial = precompiledMethod.isCompilerSpecial;
        method.returnType = resolveType(precompiledMethod.returnType, source, sideClasses);

        VariableMap localVariables = new VariableMap();
        resolveSingleExpression(precompiledMethod.instructions, currentType, source, sideClasses, localVariables).forEach(method.instructions::add);
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
        // if some methods have the same name, last one should win
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
            superclass = findClass("weac.lang.WeacObject", source, sideClasses);
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

        // TODO: better logging
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

    private List<WeacResolvedEnumConstant> resolveEnums(List<WeacPrecompiledEnumConstant> enumConstants, WeacType currentType, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        List<WeacResolvedEnumConstant> resolvedConstants = new ArrayList<>();
        VariableMap enumVarMap = new VariableMap();
        for(WeacPrecompiledEnumConstant cst : enumConstants) {
            if(enumVarMap.exists(cst.name)) {
                newError(cst.name+" enum field already exists", -1); // TODO: line
            }
            enumVarMap.register(cst.name);
            WeacResolvedEnumConstant resolved = new WeacResolvedEnumConstant();
            resolved.name = cst.name;

            cst.parameters.stream()
                    .map(m -> resolveSingleExpression(m, currentType, source, sideClasses, enumVarMap))
                    .forEach(resolved.parameters::add);

            resolvedConstants.add(resolved);
        }
        return resolvedConstants;
    }

    private List<WeacResolvedInsn> resolveSingleExpression(List<WeacPrecompiledInsn> precompiled, WeacType currentType, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses, VariableMap varMap) {
        List<WeacResolvedInsn> insns = new LinkedList<>();
        Stack<WeacResolvedInsn> stack = new Stack<>();
        Stack<WeacValue> valueStack = new Stack<>();
        for(int i = 0;i<precompiled.size();i++) {
            WeacPrecompiledInsn precompiledInsn = precompiled.get(i);
            if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_NUMBER_CONSTANT) {
                WeacLoadNumberConstant cst = ((WeacLoadNumberConstant) precompiledInsn);
                String numberRepresentation = cst.getValue();
                WeacResolvedInsn resolvedNumber = numberResolver.resolve(numberRepresentation);

                WeacType numberType = null;
                valueStack.push(new WeacConstantValue(numberType));

                stack.push(resolvedNumber);
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_BOOLEAN_CONSTANT) {
                WeacLoadBooleanConstant cst = ((WeacLoadBooleanConstant) precompiledInsn);
                stack.push(new WeacLoadBooleanInsn(cst.getValue()));

                valueStack.push(new WeacConstantValue(WeacType.BOOLEAN_TYPE));
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_STRING_CONSTANT) {
                WeacLoadStringConstant cst = ((WeacLoadStringConstant) precompiledInsn);
                stack.push(stringResolver.resolve(cst.getValue()));
                valueStack.push(new WeacConstantValue(new WeacType("java.lang.String", true)));
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_CHARACTER_CONSTANT) {
                WeacLoadCharacterConstant cst = ((WeacLoadCharacterConstant) precompiledInsn);
                stack.push(new WeacLoadCharInsn(stringResolver.resolveSingleCharacter(cst.getValue().toCharArray(), 0)));
                valueStack.push(new WeacConstantValue(WeacType.CHAR_TYPE));
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LABEL) {
                WeacLabelInsn cst = ((WeacLabelInsn) precompiledInsn);
                stack.push(new WeacResolvedLabelInsn(cst.getLabel()));
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_VARIABLE) {
                WeacLoadVariable ldVar = ((WeacLoadVariable) precompiledInsn);
                String varName = ldVar.getName();
                if(varName.equals("this")) {
                    stack.push(new WeacLoadThisInsn());
                } else {
                    // check if local variable
                    if(varMap.exists(varName)) {
                        stack.push(new WeacLoadVariableInsn(varMap.getIndex(varName)));
                    } else {
                        // check if field

                        // check if object instance

                    }
                }
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.FUNCTION_CALL) {
                WeacFunctionCall cst = ((WeacFunctionCall) precompiledInsn);
                WeacType owner = findFunctionOwner(stack, currentType, cst, source, sideClasses);


                // TODO: find real method

                WeacType[] argTypes = new WeacType[0];
                WeacType returnType = WeacType.BOOLEAN_TYPE; // TODO
                stack.push(new WeacFunctionCallInsn(cst.getName(), owner, cst.getArgCount(), cst.shouldLookForInstance(), argTypes, returnType));
                int toPop = cst.getArgCount() + (cst.shouldLookForInstance() ? 1 : 0);
                for(int _dontcare = 0;_dontcare<toPop;_dontcare++) {
                    valueStack.pop();
                }
                if(!returnType.equals(WeacType.VOID_TYPE))
                    valueStack.push(new WeacConstantValue(returnType));
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.RETURN) {
                WeacPrecompiledInsn cst = ((WeacPrecompiledInsn) precompiledInsn);
                if(valueStack.isEmpty()) {
                    insns.add(new WeacResolvedInsn(ResolveOpcodes.RETURN));
                } else {
                    WeacType type = valueStack.pop().getType();
                    int opcode = ResolveOpcodes.OBJ_RETURN;
                    if(type == null) {
                        opcode = ResolveOpcodes.RETURN;
                    } else if(type.equals(WeacType.BOOLEAN_TYPE)) {
                        opcode = ResolveOpcodes.BOOL_RETURN;
                    } else if(type.equals(WeacType.CHAR_TYPE)) {
                        opcode = ResolveOpcodes.CHAR_RETURN;
                    } else if(type.equals(WeacType.DOUBLE_TYPE)) {
                        opcode = ResolveOpcodes.DOUBLE_RETURN;
                    } else if(type.equals(WeacType.FLOAT_TYPE)) {
                        opcode = ResolveOpcodes.FLOAT_RETURN;
                    } else if(type.equals(WeacType.INTEGER_TYPE)) {
                        opcode = ResolveOpcodes.INT_RETURN;
                    } else if(type.equals(WeacType.LONG_TYPE)) {
                        opcode = ResolveOpcodes.LONG_RETURN;
                    } else if(type.equals(WeacType.SHORT_TYPE)) {
                        opcode = ResolveOpcodes.SHORT_RETURN;
                    } else if(type.equals(WeacType.VOID_TYPE)) {
                        opcode = ResolveOpcodes.RETURN;
                    }

                    if(opcode != ResolveOpcodes.RETURN) {
                        insns.add(stack.pop());
                    }

                    insns.add(new WeacResolvedInsn(opcode));
                }
            }
        }
        return insns;
    }

    private WeacType findFunctionOwner(Stack<WeacResolvedInsn> stack, WeacType currentType, WeacFunctionCall cst, WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        if(cst.shouldLookForInstance()) {

        } else {
            return currentType;
        }
        return null;
    }

}
