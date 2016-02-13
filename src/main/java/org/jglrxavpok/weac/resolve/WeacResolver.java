package org.jglrxavpok.weac.resolve;

import org.jglrxavpok.weac.WeacCompileUtils;
import org.jglrxavpok.weac.parse.EnumClassTypes;
import org.jglrxavpok.weac.patterns.WeacInstructionPattern;
import org.jglrxavpok.weac.precompile.insn.*;
import org.jglrxavpok.weac.precompile.structure.*;
import org.jglrxavpok.weac.resolve.insn.*;
import org.jglrxavpok.weac.resolve.structure.*;
import org.jglrxavpok.weac.resolve.values.*;
import org.jglrxavpok.weac.utils.EnumOperators;
import org.jglrxavpok.weac.utils.Identifier;
import org.jglrxavpok.weac.utils.WeacImport;
import org.jglrxavpok.weac.utils.WeacType;

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

    public WeacResolvedSource process(WeacResolvingContext context) {
        WeacResolvedSource resolved = new WeacResolvedSource();
        resolved.packageName = context.getSource().packageName;
        resolved.classes = new ArrayList<>();

        context.getSource().classes.forEach(c -> resolved.classes.add(resolve(c, context)));
        return resolved;
    }

    private WeacResolvedClass resolve(WeacPrecompiledClass aClass, WeacResolvingContext context) {
        WeacResolvedClass resolvedClass = new WeacResolvedClass();
        WeacType currentType = resolveType(new Identifier(aClass.name), context);
        resolvedClass.access = aClass.access;
        resolvedClass.annotations.addAll(resolveAnnotations(aClass.annotations, currentType, context));
        resolvedClass.classType = aClass.classType;
        resolvedClass.parents = getHierarchy(aClass, aClass.interfacesImplemented, context);
        resolvedClass.fullName = getFullName(aClass);
        resolvedClass.enumConstants = resolveEnums(aClass.enumConstants, currentType, context);
        resolvedClass.isAbstract = aClass.isAbstract;
        resolvedClass.isMixin = aClass.isMixin;
        resolvedClass.name = aClass.name;
        resolvedClass.isCompilerSpecial = aClass.isCompilerSpecial;
        resolvedClass.isFinal = aClass.isFinal;

        WeacMixedContentClass toMixIn = resolveMixins(resolvedClass, resolvedClass.parents.getMixins());
        resolvedClass.methods = resolveMethods(currentType, aClass, context, toMixIn);

        resolvedClass.fields = resolveFields(resolvedClass, currentType, aClass, context, toMixIn);
        return resolvedClass;
    }

    private List<WeacResolvedAnnotation> resolveAnnotations(List<WeacPrecompiledAnnotation> annotations, WeacType currentType, WeacResolvingContext context) {
        List<WeacResolvedAnnotation> resolvedAnnotations = new LinkedList<>();
        for(WeacPrecompiledAnnotation a : annotations) {
            WeacPrecompiledSource annotSource = new WeacPrecompiledSource();
            WeacPrecompiledClass clazz = findClass(a.getName(), context);
            annotSource.classes = new LinkedList<>();
            annotSource.classes.add(clazz);
            annotSource.classes.addAll(context.getSource().classes); // corner cases where the annotation uses the type it is describing

            annotSource.imports = new LinkedList<>();
            if(clazz == null)
                throw new RuntimeException(":cc "+a.getName());
            annotSource.packageName = clazz.packageName;

            WeacResolvedAnnotation resolved = new WeacResolvedAnnotation(resolve(clazz, context));
            a.args.stream()
                    .map(l -> resolveSingleExpression(l, currentType, context, new VariableMap()))
                    .forEach(resolved.getArgs()::add);
            resolvedAnnotations.add(resolved);
        }
        return resolvedAnnotations;
    }

    private List<WeacResolvedField> resolveFields(WeacResolvedClass resolvedClass, WeacType currentType, WeacPrecompiledClass aClass, WeacResolvingContext context, WeacMixedContentClass toMixIn) {
        if(resolvedClass.isMixin) {
            return Collections.emptyList();
        }
        List<WeacResolvedField> fields = new LinkedList<>();
        VariableMap fieldVarMap = new VariableMap();
        toMixIn.fields.forEach(m -> addOrOverrideField(resolveSingleField(m, currentType, context, fieldVarMap), fields));

        aClass.fields.forEach(m -> addOrOverrideField(resolveSingleField(m, currentType, context, fieldVarMap), fields));
        return fields;
    }

    private WeacResolvedField resolveSingleField(WeacPrecompiledField field, WeacType currentType, WeacResolvingContext context, VariableMap fieldVarMap) {
        WeacResolvedField resolvedField = new WeacResolvedField();
        resolvedField.name = field.name;
        resolvedField.access = field.access;
        resolvedField.isCompilerSpecial = field.isCompilerSpecial;
        resolvedField.type = resolveType(field.type, context);
        resolvedField.defaultValue.addAll(resolveSingleExpression(field.defaultValue, currentType, context, fieldVarMap));
        return resolvedField;
    }

    private List<WeacResolvedMethod> resolveMethods(WeacType currentType, WeacPrecompiledClass aClass, WeacResolvingContext context, WeacMixedContentClass toMixIn) {
        List<WeacResolvedMethod> methods = new LinkedList<>();
        toMixIn.methods.forEach(m -> addOrOverride(resolveSingleMethod(m, currentType, context, aClass), methods));

        aClass.methods.forEach(m -> addOrOverride(resolveSingleMethod(m, currentType, context, aClass), methods));
        return methods;
    }

    private WeacResolvedMethod resolveSingleMethod(WeacPrecompiledMethod precompiledMethod, WeacType currentType, WeacResolvingContext context, WeacPrecompiledClass precompiledClass) {
        WeacResolvedMethod method = new WeacResolvedMethod();
        method.access = precompiledMethod.access;
        method.annotations.addAll(resolveAnnotations(precompiledMethod.annotations, currentType, context));
        method.argumentNames.addAll(precompiledMethod.argumentNames);
        method.isAbstract = precompiledMethod.isAbstract;
        method.isConstructor = precompiledMethod.isConstructor;
        method.name = precompiledMethod.name;
        String mName = method.name.getId();
        if(mName.startsWith("operator") || mName.startsWith("unary")) {
            boolean unary = mName.startsWith("unary");
            int endIndex;
            if(unary) {
                endIndex = "unary".length();
            } else {
                endIndex = "operator".length();
            }
            method.overloadOperator = EnumOperators.get(mName.substring(endIndex), unary);
        }
        method.isCompilerSpecial = precompiledMethod.isCompilerSpecial;
        method.returnType = resolveType(precompiledMethod.returnType, context);

        precompiledMethod.argumentTypes.stream()
                .map(t -> resolveType(t, context))
                .forEach(method.argumentTypes::add);

        VariableMap localVariables = new VariableMap();
        registerFields(localVariables, precompiledClass, context);
        localVariables.registerLocal("this", currentType);
        for(int i = 0;i<method.argumentNames.size();i++) {
            Identifier name = method.argumentNames.get(i);
            WeacType type = method.argumentTypes.get(i);
            localVariables.registerLocal(name.getId(), type);
        }
        resolveSingleExpression(precompiledMethod.instructions, currentType, context, localVariables).forEach(method.instructions::add);
        return method;
    }

    private void registerFields(VariableMap variables, WeacPrecompiledClass precompiledClass, WeacResolvingContext context) {
        for(WeacPrecompiledField f : precompiledClass.fields) {
            WeacType type = resolveType(f.type, context);
            String name = f.name.getId();
            variables.registerField(name, type);
        }
    }

    private WeacType resolveType(Identifier type, WeacResolvingContext context) {
        WeacType primitiveType = getPotentialPrimitive(type);
        if(primitiveType != null) {
            return primitiveType;
        }

        WeacType intermediateType = new WeacType(null, type.getId(), true);
        String core = intermediateType.getCoreType().getIdentifier().getId();

        WeacType primitive = getPotentialPrimitive(new Identifier(core));
        if(primitive != null) {
            return new WeacType(WeacType.PRIMITIVE_TYPE, primitive.getIdentifier().getId()+(type.getId().substring(core.length())), true);
        }
        WeacPrecompiledClass typeClass = findClass(core, context);
        if(typeClass == null) {
            newError("Invalid type: "+type.getId()+" in "+context.getSource().classes.get(0).fullName, -1);
        }

        WeacType superclass = null;
        if (typeClass != null && !typeClass.fullName.equals(WeacType.OBJECT_TYPE.getIdentifier().toString()) // WeacObject
                && !typeClass.fullName.equals(WeacType.JOBJECT_TYPE.getIdentifier().toString())) { // Java's Object
            String superclassName = typeClass.motherClass;
            if(superclassName != null) {
                WeacPrecompiledClass superTypeClass = findClass(superclassName, context);
                if (superTypeClass != null && superTypeClass.name != null) {
                    superclass = resolveType(new Identifier(superTypeClass.fullName, true), context);
                }
            }
        }
        return new WeacType(superclass, typeClass.fullName+(type.getId().substring(core.length())), true);
    }

    private WeacType getPotentialPrimitive(Identifier type) {

        switch (type.getId()) {
            case "void":
                return WeacType.VOID_TYPE;

            case "boolean":
                return WeacType.BOOLEAN_TYPE;

            case "byte":
                return WeacType.BYTE_TYPE;

            case "char":
                return WeacType.CHAR_TYPE;

            case "double":
                return WeacType.DOUBLE_TYPE;

            case "float":
                return WeacType.FLOAT_TYPE;

            case "int":
                return WeacType.INTEGER_TYPE;

            case "long":
                return WeacType.LONG_TYPE;

            case "short":
                return WeacType.SHORT_TYPE;
        }
        return null;
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

    private ClassHierarchy getHierarchy(WeacPrecompiledClass aClass, List<String> interfacesImplemented, WeacResolvingContext context) {
        ClassHierarchy parents = new ClassHierarchy();

        List<String> interfaces = new ArrayList<>();
        interfaces.addAll(interfacesImplemented);

        if(!interfaces.contains(aClass.motherClass) && aClass.motherClass != null)
            interfaces.add(aClass.motherClass);

        WeacPrecompiledClass superclass = null;
        boolean isPresent = context.getSource().classes.stream().filter(c -> c.fullName.equals(aClass.fullName)).count() != 0L;
        WeacResolvingContext newContext;
        if(!isPresent) {
            WeacPrecompiledSource newSource = new WeacPrecompiledSource();
            newSource.classes = new LinkedList<>();
            newSource.classes.add(aClass);

            newSource.imports = aClass.imports;
            newSource.packageName = aClass.packageName;
            newContext = new WeacResolvingContext(newSource, context.getSideClasses());
        } else {
            newContext = context;
        }
        for(String inter : interfaces) {
            WeacPrecompiledClass clazz = findClass(inter, newContext);
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
            superclass = findClass("weac.lang.WeacObject", context);
        }
        parents.setSuperclass(superclass);
        return parents;
    }

    private WeacPrecompiledClass findClass(String inter, WeacResolvingContext context) {
        String transformedName = transform(inter, context.getSource().imports);
        //System.out.println(inter+" transformed to "+transformedName+" with "+context.getSource().classes.get(0).fullName);
        // check in source if can be found
        for (WeacPrecompiledClass sourceClass : context.getSource().classes) {
            if(sourceClass.fullName.equals(transformedName)) {
                return sourceClass;
            }

            if(sourceClass.name.equals(inter)) {
                return sourceClass;
            }
        }

        for (WeacPrecompiledClass sideClass : context.getSideClasses()) {
            if(sideClass.fullName.equals(transformedName)) {
                return sideClass;
            }
        }

        // test for base classes, because they are not necessarily imported
        for (WeacPrecompiledClass sideClass : context.getSideClasses()) {
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
//            e.printStackTrace();

            // try importing from java.lang
            try {
                javaClass = Class.forName("java.lang."+inter, false, getClass().getClassLoader());
                if(javaClass != null) {
                    return new JavaImportedClass(javaClass);
                }
            } catch (ClassNotFoundException e1) {
                throw new RuntimeException(inter+" / "+context.getSource().classes.get(0).fullName, e1);
            }
        }

        // TODO: better logging
        System.err.println("NOT FOUND: \""+inter+"\""+" / \""+transformedName+"\"");

        return null;
    }

    private String transform(String name, List<WeacImport> imports) {
        if(imports.isEmpty()) {
            //System.err.println("EMPTY IMPORTS ME IS SAD ;(");
            //new Exception(name).printStackTrace();
        }
        for(WeacImport imp : imports) {
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

    private List<WeacResolvedEnumConstant> resolveEnums(List<WeacPrecompiledEnumConstant> enumConstants, WeacType currentType, WeacResolvingContext context) {
        List<WeacResolvedEnumConstant> resolvedConstants = new ArrayList<>();
        VariableMap enumVarMap = new VariableMap();
        for(WeacPrecompiledEnumConstant cst : enumConstants) {
            if(enumVarMap.localExists(cst.name)) {
                newError(cst.name+" enum field already localExists", -1); // TODO: line
            }
            enumVarMap.registerLocal(cst.name, currentType);
            WeacResolvedEnumConstant resolved = new WeacResolvedEnumConstant();
            resolved.name = cst.name;

            cst.parameters.stream()
                    .map(m -> resolveSingleExpression(m, currentType, context, enumVarMap))
                    .forEach(resolved.parameters::add);

            resolvedConstants.add(resolved);
        }
        return resolvedConstants;
    }

    private List<WeacResolvedInsn> resolveSingleExpression(List<WeacPrecompiledInsn> precompiled, WeacType selfType, WeacResolvingContext context, VariableMap varMap) {
        List<WeacResolvedInsn> insns = new LinkedList<>();
        Stack<WeacValue> valueStack = new Stack<>();

        Map<WeacType, VariableMap> variableMaps = new HashMap<>();
        // fill variableMaps
        for(WeacPrecompiledClass cl : context.getSideClasses()) {
            registerVariables(cl, variableMaps, context);
        }
        variableMaps.put(selfType, varMap);

        WeacType currentVarType = selfType;
        boolean currentIsStatic = false;
        for(int i = 0;i<precompiled.size();i++) {
            WeacPrecompiledInsn precompiledInsn = precompiled.get(i);
            if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_NUMBER_CONSTANT) {
                WeacLoadNumberConstant cst = ((WeacLoadNumberConstant) precompiledInsn);
                String numberRepresentation = cst.getValue();
                WeacResolvedInsn resolvedNumber = numberResolver.resolve(numberRepresentation);

                WeacType numberType = extractType(resolvedNumber);

                valueStack.push(new WeacConstantValue(numberType));

                insns.add(resolvedNumber);

                currentVarType = numberType;
                currentIsStatic = false;
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_BOOLEAN_CONSTANT) {
                WeacLoadBooleanConstant cst = ((WeacLoadBooleanConstant) precompiledInsn);
                insns.add(new WeacLoadBooleanInsn(cst.getValue()));

                valueStack.push(new WeacConstantValue(WeacType.BOOLEAN_TYPE));
                currentVarType = WeacType.BOOLEAN_TYPE;
                currentIsStatic = false;
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_STRING_CONSTANT) {
                WeacLoadStringConstant cst = ((WeacLoadStringConstant) precompiledInsn);
                insns.add(stringResolver.resolve(cst.getValue()));
                valueStack.push(new WeacConstantValue(WeacType.STRING_TYPE));

                currentVarType = WeacType.STRING_TYPE;
                currentIsStatic = false;
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_CHARACTER_CONSTANT) {
                WeacLoadCharacterConstant cst = ((WeacLoadCharacterConstant) precompiledInsn);
                insns.add(new WeacLoadCharInsn(stringResolver.resolveSingleCharacter(cst.getValue().toCharArray(), 0)));
                valueStack.push(new WeacConstantValue(WeacType.CHAR_TYPE));

                currentVarType = WeacType.CHAR_TYPE;
                currentIsStatic = false;
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LABEL) {
                WeacLabelInsn cst = ((WeacLabelInsn) precompiledInsn);
                insns.add(new WeacResolvedLabelInsn(cst.getLabel()));
                currentVarType = selfType;
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.ARGUMENT_SEPARATOR) {
                currentVarType = selfType;
                currentIsStatic = false;
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_VARIABLE) {
                WeacLoadVariable ldVar = ((WeacLoadVariable) precompiledInsn);
                String varName = ldVar.getName();
                if(varName.equals("this")) {
                    insns.add(new WeacLoadThisInsn());
                    valueStack.push(new WeacThisValue(selfType));
                    currentVarType = selfType;
                    currentIsStatic = false;
                } else {
                    // check if local variable
                    if(varMap.localExists(varName)) {
                        int index = varMap.getLocalIndex(varName);
                        insns.add(new WeacLoadVariableInsn(index));
                        WeacType localType = varMap.getLocalType(varName);
                        valueStack.push(new WeacVariableValue(varName, localType, index));
                        currentVarType = localType;
                        currentIsStatic = false;
                    } else {
                        // check if field
                        if(!variableMaps.containsKey(currentVarType)) {
                            WeacPrecompiledClass clazz = findClass(currentVarType.getIdentifier().getId(), context);
                            registerVariables(clazz, variableMaps, context);
                        }
                        if(variableMaps.get(currentVarType).fieldExists(varName)) {
                            System.out.println("PL FOUND in "+currentVarType.getIdentifier()+" : "+varName);
                            WeacType fieldType = variableMaps.get(currentVarType).getFieldType(varName);
                            insns.add(new WeacLoadFieldInsn(varName, currentVarType, fieldType, currentIsStatic));
                            valueStack.push(new WeacFieldValue(varName, currentVarType, fieldType));
                            currentVarType = fieldType;
                            currentIsStatic = false;
                        } else {
                            // TODO: Check for objects
                            WeacPrecompiledClass clazz = findClass(varName, context);
                            if(clazz != null) {
                                currentVarType = resolveType(new Identifier(clazz.fullName, true), context);
                                if(clazz instanceof JavaImportedClass) {
                                    currentIsStatic = true;
                                    valueStack.push(new WeacConstantValue(currentVarType));
                                } else if(clazz.classType == EnumClassTypes.OBJECT) {
                                    insns.add(new WeacLoadFieldInsn("__instance__", currentVarType, currentVarType, true));
                                    valueStack.push(new WeacFieldValue("__instance__", currentVarType, currentVarType));
                                    currentIsStatic = false;
                                } else {
                                    newError(":cc2 "+context.getSource().classes.get(0).fullName+" / "+currentVarType.getIdentifier(), -1); // todo line
                                }
                            } else {
                                newError(":cc "+context.getSource().classes.get(0).fullName+" / "+varName+" / "+currentVarType, -1); // todo line
                            }
                        }

                        // check if object instance

                    }
                }
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.FUNCTION_CALL) {
                WeacFunctionCall cst = ((WeacFunctionCall) precompiledInsn);
                WeacValue owner;
                String name;
                if(cst.getName().equals("this")) { // call constructor
                    owner = new WeacThisValue(selfType);
                    name = selfType.getIdentifier().getId();
                } else if(cst.getName().equals("super")) { // call super constructor
                    owner = new WeacThisValue(selfType.getSuperType());
                    name = selfType.getSuperType().getIdentifier().getId();
                } else {
                    owner = findFunctionOwner(valueStack, selfType, cst, context);
                    name = cst.getName();
                }

                WeacType[] argTypes = new WeacType[cst.getArgCount()];

                WeacPrecompiledMethod realMethod = findMethod(owner.getType(), name, cst.getArgCount(), valueStack, context);

                for(int i0 = 0;i0<argTypes.length;i0++) {
                    argTypes[i0] = resolveType(realMethod.argumentTypes.get(i0), context);
                }

                WeacType returnType = resolveType(realMethod.returnType, context);
                insns.add(new WeacFunctionCallInsn(name, owner.getType(), cst.getArgCount(), cst.shouldLookForInstance() && !realMethod.isJavaImported, argTypes, returnType));
                int toPop = cst.getArgCount() + ((cst.shouldLookForInstance() && !realMethod.isJavaImported) ? 1 : 0);
                if(valueStack.size() < toPop) {
                    throw new RuntimeException(name+" / "+toPop+" "+Arrays.toString(valueStack.toArray())+" / "+cst.shouldLookForInstance()+" (java: "+realMethod.isJavaImported+")");
                }
                for(int _dontcare = 0;_dontcare<toPop;_dontcare++) {
                    valueStack.pop();
                }
                if(!returnType.equals(WeacType.VOID_TYPE))
                    valueStack.push(new WeacConstantValue(returnType));
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.RETURN) {
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

                    insns.add(new WeacResolvedInsn(opcode));
                }
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.THIS) {
                insns.add(new WeacLoadThisInsn());
                valueStack.push(new WeacThisValue(selfType));
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_NULL) {
                insns.add(new WeacLoadNullInsn());
                valueStack.push(new WeacNullValue());
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LABEL) {
                while(!valueStack.isEmpty()) {
                    valueStack.pop();
                    insns.add(new WeacPopInsn());
                }
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.BINARY_OPERATOR) {
                WeacOperatorInsn insn = ((WeacOperatorInsn) precompiledInsn);
                EnumOperators op = insn.getOperator();
                switch (op) {
                    case SET_TO:

                        WeacValue toAssign = valueStack.pop();
                        WeacValue potentialVariable = valueStack.pop();
                        if(potentialVariable.isConstant()) {
                            newError(potentialVariable.getName()+"Invalid assigment, can only assign a value to a field or a variable", -1); // todo line
                        } else {
                            String name = potentialVariable.getName();
                            if(varMap.localExists(name)) { // local variables take priority
                                insns.add(new WeacStoreVarInsn(varMap.getLocalIndex(name)));
                            } else if(varMap.fieldExists(name)) {
                                insns.add(new WeacStoreFieldInsn(name, varMap.getFieldType(name)));
                            } else {
                                newError("local or field "+name+" does not exist", -1); // todo line
                            }
                        }
                        break;

                    default:
                        System.err.println("UNRESOLVED OP: "+op.name());
                }
            } else {
                System.err.println("UNRESOLVED: "+precompiledInsn);
            }
        }
        return insns;
    }

    private void registerVariables(WeacPrecompiledClass cl, Map<WeacType, VariableMap> variableMaps, WeacResolvingContext context) {
        WeacType type = resolveType(new Identifier(cl.fullName, true), context);
        VariableMap map = new VariableMap();
        for(WeacPrecompiledField f : cl.fields) {
            WeacType fieldType = null;
            if(cl instanceof JavaImportedClass) {
                fieldType = JavaImportedClass.toWeacType(f.type.getId());
            }
            if(fieldType == null)
                fieldType = resolveType(f.type, context);
            map.registerField(f.name.getId(), fieldType);
        }
        variableMaps.put(type, map);
    }

    private WeacPrecompiledMethod findMethod(WeacType topType, String name, int argCount, Stack<WeacValue> valueStack, WeacResolvingContext context) {
        if((valueStack.size()-(argCount-1)-1) < 0) {
            System.out.println("DZDQZD "+name+" "+argCount+Arrays.toString(valueStack.toArray()));
        }
        WeacType[] potentialArgTypes = new WeacType[argCount];
        for(int i = 0;i<argCount;i++) {
            int index = valueStack.size()-(argCount-i-1)-1;
            potentialArgTypes[i] = valueStack.get(index).getType();
        }
        return findMethodFromHierarchy(topType, name, argCount, potentialArgTypes, context);
    }

    private WeacPrecompiledMethod findMethodFromHierarchy(WeacType topType, String name, int argCount, WeacType[] argTypes, WeacResolvingContext context) {
        WeacPrecompiledClass topClass = findClass(topType.getIdentifier().getId(), context);
        Optional<WeacPrecompiledMethod> foundMethod = topClass.methods.stream()
                .filter(m -> m.name.getId().equals(name) || (m.isConstructor && topType.getIdentifier().getId().endsWith(name)))
                .filter(m -> m.argumentNames.size() == argCount)
                .filter(m -> {
                    for(int i = 0;i<argTypes.length;i++) {
                        WeacType argType = argTypes[i];
                        WeacType paramType = resolveType(m.argumentTypes.get(i), context);
                        if(!isCastable(argType, paramType, context)) {
                            System.out.println(argType+" NOT CAST "+paramType);
                            return false; // TODO: test castable
                        } else {
                            System.out.println(argType+" is castable to "+paramType);
                        }
                    }
                    // TODO: check if argument types match
                    return true;
                })
                .sorted((a, b) -> {
                    // TODO: make methods that have the correct types go towards the first indexes
                    return 0;
                })
                .findFirst();
        if(foundMethod.isPresent()) {
            return foundMethod.get();
        }

        boolean isPresent = context.getSource().classes.stream().filter(c -> c.fullName.equals(topClass.fullName)).count() != 0L;
        WeacResolvingContext newContext;
        if(!isPresent) {
            WeacPrecompiledSource newSource = new WeacPrecompiledSource();
            newSource.classes = new LinkedList<>();
            newSource.classes.add(topClass);
            newSource.imports = topClass.imports;
            newSource.packageName = topClass.packageName;
            newContext = new WeacResolvingContext(newSource, context.getSideClasses());
        } else {
            newContext = context;
        }

        ClassHierarchy hierarchy = getHierarchy(topClass, topClass.interfacesImplemented, newContext);

        WeacPrecompiledClass superclass = hierarchy.getSuperclass();
        if(superclass != null && !superclass.fullName.equals(topClass.fullName)) {
            WeacType superType = resolveType(new Identifier(superclass.fullName, true), context);
            WeacPrecompiledMethod supermethod = findMethodFromHierarchy(superType, name, argCount, argTypes, context);
            if(supermethod == null) {
                // TODO: check interfaces
                List<WeacPrecompiledClass> interfaces = hierarchy.getInterfaces();
                for(WeacPrecompiledClass in : interfaces) {
                    WeacType interType = resolveType(new Identifier(in.fullName, true), context);
                    WeacPrecompiledMethod m = findMethodFromHierarchy(interType, name, argCount, argTypes, context);
                    if(m != null) {
                        return m;
                    }
                }

                if(superType.equals(WeacType.OBJECT_TYPE) || superType.equals(WeacType.JOBJECT_TYPE)) {
                    return null;
                }
            } else {
                return supermethod;
            }
        }

        return null;
    }

    private boolean isCastable(WeacType from, WeacType to, WeacResolvingContext context) {
        if(from.isArray() || to.isArray()) {
            return from.equals(to); // TODO: Implicit array casts?
        }
        if(from.equals(to)) {
            return true;
        }
        // try primitive types

        WeacPrecompiledClass fromClass = findClass(from.getIdentifier().getId(), context);
        WeacPrecompiledClass toClass = findClass(to.getIdentifier().getId(), context);
        return isCastable(fromClass, toClass, context);
    }

    private boolean isCastable(WeacPrecompiledClass from, WeacPrecompiledClass to, WeacResolvingContext context) {
        if(from.fullName.equals("java.lang.Object") && !to.fullName.equals("java.lang.Object"))
            return false;
        ClassHierarchy hierarchy = getHierarchy(from, from.interfacesImplemented, context);

        // check superclass
        if(hierarchy.getSuperclass().equals(to)) {
            return true;
        }

        // check interfaces
        for(WeacPrecompiledClass inter : hierarchy.getInterfaces()) {
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

    private WeacType extractType(WeacResolvedInsn number) {
        switch (number.getOpcode()) {
            case ResolveOpcodes.LOAD_BYTE_CONSTANT:
                return WeacType.BYTE_TYPE;

            case ResolveOpcodes.LOAD_DOUBLE_CONSTANT:
                return WeacType.DOUBLE_TYPE;

            case ResolveOpcodes.LOAD_FLOAT_CONSTANT:
                return WeacType.FLOAT_TYPE;

            case ResolveOpcodes.LOAD_INTEGER_CONSTANT:
                return WeacType.INTEGER_TYPE;

            case ResolveOpcodes.LOAD_LONG_CONSTANT:
                return WeacType.LONG_TYPE;

            case ResolveOpcodes.LOAD_SHORT_CONSTANT:
                return WeacType.SHORT_TYPE;

        }
        System.out.printf("num: "+number);
        return WeacType.VOID_TYPE;
    }

    private WeacValue findFunctionOwner(Stack<WeacValue> stack, WeacType currentType, WeacFunctionCall cst, WeacResolvingContext context) {
        if(cst.shouldLookForInstance()) {
            System.out.println("OWNER: "+currentType.getIdentifier()+" "+cst+" "+Arrays.toString(stack.toArray()));
            int args = cst.getArgCount();
            int index = stack.size()-args-1;
            return stack.get(index);
        } else {
            return new WeacThisValue(currentType);
        }
    }

}
