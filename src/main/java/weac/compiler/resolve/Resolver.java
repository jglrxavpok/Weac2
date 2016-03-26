package weac.compiler.resolve;

import org.jglr.flows.collection.VariableTopStack;
import weac.compiler.CompileUtils;
import weac.compiler.parse.EnumClassTypes;
import weac.compiler.patterns.InstructionPattern;
import weac.compiler.precompile.insn.*;
import weac.compiler.precompile.structure.*;
import weac.compiler.resolve.insn.*;
import weac.compiler.resolve.structure.*;
import weac.compiler.resolve.values.*;
import weac.compiler.utils.*;

import java.util.*;

public class Resolver extends CompileUtils {

    private final List<InstructionPattern<ResolvedInsn>> patterns;
    private final NumberResolver numberResolver;
    private final StringResolver stringResolver;
    private final PrimitiveCastSolver primitiveCasts;

    public Resolver() {
        patterns = new LinkedList<>();
        numberResolver = new NumberResolver();
        stringResolver = new StringResolver();
        primitiveCasts = new PrimitiveCastSolver();
    }

    public ResolvedSource process(ResolvingContext context) {
        ResolvedSource resolved = new ResolvedSource();
        resolved.fileName = context.getSource().fileName;
        resolved.packageName = context.getSource().packageName;
        resolved.classes = new ArrayList<>();

        context.getSource().classes.forEach(c -> resolved.classes.add(resolve(c, context)));
        return resolved;
    }

    private ResolvedClass resolve(PrecompiledClass aClass, ResolvingContext context) {
        ResolvedClass resolvedClass = new ResolvedClass();
        WeacType currentType = resolveType(aClass.name.getIdentifier(), context);
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

        MixedContentClass toMixIn = resolveMixins(resolvedClass, resolvedClass.parents.getMixins());
        resolvedClass.fields = resolveFields(resolvedClass, currentType, aClass, context, toMixIn);

        resolvedClass.methods = resolveMethods(currentType, aClass, context, toMixIn);

        return resolvedClass;
    }

    private List<ResolvedAnnotation> resolveAnnotations(List<PrecompiledAnnotation> annotations, WeacType currentType, ResolvingContext context) {
        List<ResolvedAnnotation> resolvedAnnotations = new LinkedList<>();
        for(PrecompiledAnnotation a : annotations) {
            PrecompiledSource annotSource = new PrecompiledSource();
            PrecompiledClass clazz = findClass(a.getName(), context);
            annotSource.classes = new LinkedList<>();
            annotSource.classes.add(clazz);
            annotSource.classes.addAll(context.getSource().classes); // corner cases where the annotation uses the type it is describing

            annotSource.imports = new LinkedList<>();
            if(clazz == null)
                throw new RuntimeException(":cc "+a.getName());
            annotSource.packageName = clazz.packageName;

            ResolvedAnnotation resolved = new ResolvedAnnotation(resolve(clazz, context));
            a.args.stream()
                    .map(l -> resolveSingleExpression(l, currentType, context, new VariableMap()))
                    .forEach(resolved.getArgs()::add);
            resolvedAnnotations.add(resolved);
        }
        return resolvedAnnotations;
    }

    private List<ResolvedField> resolveFields(ResolvedClass resolvedClass, WeacType currentType, PrecompiledClass aClass, ResolvingContext context, MixedContentClass toMixIn) {
        if(resolvedClass.isMixin) {
            return Collections.emptyList();
        }
        List<ResolvedField> fields = new LinkedList<>();
        VariableMap fieldVarMap = new VariableMap();
        toMixIn.fields.forEach(m -> addOrOverrideField(resolveSingleField(m, currentType, context, fieldVarMap), fields));

        aClass.fields.forEach(m -> addOrOverrideField(resolveSingleField(m, currentType, context, fieldVarMap), fields));
        return fields;
    }

    private ResolvedField resolveSingleField(PrecompiledField field, WeacType currentType, ResolvingContext context, VariableMap fieldVarMap) {
        ResolvedField resolvedField = new ResolvedField();
        resolvedField.name = field.name;
        resolvedField.access = field.access;
        resolvedField.isCompilerSpecial = field.isCompilerSpecial;
        resolvedField.type = resolveType(field.type, context);
        resolvedField.defaultValue.addAll(resolveSingleExpression(field.defaultValue, currentType, context, fieldVarMap));
        return resolvedField;
    }

    private List<ResolvedMethod> resolveMethods(WeacType currentType, PrecompiledClass aClass, ResolvingContext context, MixedContentClass toMixIn) {
        List<ResolvedMethod> methods = new LinkedList<>();
        toMixIn.methods.forEach(m -> addOrOverride(resolveSingleMethod(m, currentType, context, aClass), methods));

        aClass.methods.forEach(m -> addOrOverride(resolveSingleMethod(m, currentType, context, aClass), methods));
        return methods;
    }

    private ResolvedMethod resolveSingleMethod(PrecompiledMethod precompiledMethod, WeacType currentType, ResolvingContext context, PrecompiledClass precompiledClass) {
        ResolvedMethod method = new ResolvedMethod();
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

    private void registerFields(VariableMap variables, PrecompiledClass precompiledClass, ResolvingContext context) {
        for(PrecompiledField f : precompiledClass.fields) {
            WeacType type = resolveType(f.type, context);
            String name = f.name.getId();
            variables.registerField(name, type);
        }
    }

    private WeacType resolveType(Identifier type, ResolvingContext context) {
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
        PrecompiledClass typeClass = findClass(core, context);
        if(typeClass == null) {
            newError("Invalid type: "+type.getId()+" in "+context.getSource().classes.get(0).fullName, -1);
        }

        WeacType superclass = null;
        if (typeClass != null && !typeClass.fullName.equals(WeacType.OBJECT_TYPE.getIdentifier().toString()) // WeacObject
                && !typeClass.fullName.equals(WeacType.JOBJECT_TYPE.getIdentifier().toString())) { // Java's Object
            String superclassName = typeClass.motherClass;
            if(superclassName != null) {
                PrecompiledClass superTypeClass = findClass(superclassName, context);
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

    private void addOrOverrideField(ResolvedField toAdd, List<ResolvedField> fieldList) {
        Iterator<ResolvedField> iterator = fieldList.iterator();
        while (iterator.hasNext()) {
            ResolvedField existing = iterator.next();
            if(existing.name.equals(toAdd.name)) { // same name
                iterator.remove();
            }
        }
        fieldList.add(toAdd);
    }

    private void addOrOverride(ResolvedMethod toAdd, List<ResolvedMethod> methodList) {
        // if some methods have the same name, last one should win
        Iterator<ResolvedMethod> iterator = methodList.iterator();
        while (iterator.hasNext()) {
            ResolvedMethod existing = iterator.next();
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

    private MixedContentClass resolveMixins(ResolvedClass resolvedClass, List<PrecompiledClass> mixins) {

        MixedContentClass contentClass = new MixedContentClass();

        for(PrecompiledClass mixin : mixins) {
            contentClass.methods.addAll(mixin.methods);
            contentClass.fields.addAll(mixin.fields);
        }
        return contentClass;
    }

    private String getFullName(PrecompiledClass aClass) {
        return (aClass.packageName == null || aClass.packageName.isEmpty()) ? aClass.name.getCoreType().getIdentifier().getId() : aClass.packageName+"."+aClass.name.getCoreType().getIdentifier().getId();
    }

    private ClassHierarchy getHierarchy(PrecompiledClass aClass, List<String> interfacesImplemented, ResolvingContext context) {
        ClassHierarchy parents = new ClassHierarchy();

        List<String> interfaces = new ArrayList<>();
        interfaces.addAll(interfacesImplemented);

        if(!interfaces.contains(aClass.motherClass) && aClass.motherClass != null)
            interfaces.add(aClass.motherClass);

        PrecompiledClass superclass = null;
        boolean isPresent = context.getSource().classes.stream().filter(c -> c.fullName.equals(aClass.fullName)).count() != 0L;
        ResolvingContext newContext;
        if(!isPresent) {
            PrecompiledSource newSource = new PrecompiledSource();
            newSource.classes = new LinkedList<>();
            newSource.classes.add(aClass);

            newSource.imports = aClass.imports;
            newSource.packageName = aClass.packageName;
            newContext = new ResolvingContext(newSource, context.getSideClasses());
        } else {
            newContext = context;
        }
        for(String inter : interfaces) {
            PrecompiledClass clazz = findClass(inter, newContext);
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
            superclass = findClass(Constants.BASE_CLASS, context);
        }
        parents.setSuperclass(superclass);
        return parents;
    }

    private PrecompiledClass findClass(String inter, ResolvingContext context) {
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
        if(imports.isEmpty()) {
            //System.err.println("EMPTY IMPORTS ME IS SAD ;(");
            //new Exception(name).printStackTrace();
        }
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

    private List<ResolvedEnumConstant> resolveEnums(List<PrecompiledEnumConstant> enumConstants, WeacType currentType, ResolvingContext context) {
        List<ResolvedEnumConstant> resolvedConstants = new ArrayList<>();
        VariableMap enumVarMap = new VariableMap();
        for(PrecompiledEnumConstant cst : enumConstants) {
            if(enumVarMap.localExists(cst.name)) {
                newError(cst.name+" enum field already localExists", -1); // TODO: line
            }
            enumVarMap.registerLocal(cst.name, currentType);
            ResolvedEnumConstant resolved = new ResolvedEnumConstant();
            resolved.name = cst.name;

            cst.parameters.stream()
                    .map(m -> resolveSingleExpression(m, currentType, context, enumVarMap))
                    .forEach(resolved.parameters::add);

            resolvedConstants.add(resolved);
        }
        return resolvedConstants;
    }

    private List<ResolvedInsn> resolveSingleExpression(List<PrecompiledInsn> precompiled, WeacType selfType, ResolvingContext context, VariableMap varMap) {
        List<ResolvedInsn> insns = new LinkedList<>();
        Stack<Value> valueStack = new Stack<>();
        VariableTopStack<Boolean> staticness = new VariableTopStack<>();
        staticness.setCurrent(false).push();

        Map<WeacType, VariableMap> variableMaps = new HashMap<>();
        // fill variableMaps
        for(PrecompiledClass cl : context.getSideClasses()) {
            registerVariables(cl, variableMaps, context);
        }
        variableMaps.put(selfType, varMap);

        WeacType currentVarType = selfType;
        for(int i = 0;i<precompiled.size();i++) {
            PrecompiledInsn precompiledInsn = precompiled.get(i);
            if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_NUMBER_CONSTANT) {
                LoadNumberConstant cst = ((LoadNumberConstant) precompiledInsn);
                String numberRepresentation = cst.getValue();
                ResolvedInsn resolvedNumber = numberResolver.resolve(numberRepresentation);

                WeacType numberType = extractType(resolvedNumber);

                valueStack.push(new ConstantValue(numberType));

                insns.add(resolvedNumber);

                currentVarType = numberType;
                staticness.setCurrent(false).push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.FUNCTION_START) {
                currentVarType = selfType;
                staticness.setCurrent(true).push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_BOOLEAN_CONSTANT) {
                LoadBooleanConstant cst = ((LoadBooleanConstant) precompiledInsn);
                insns.add(new LoadBooleanInsn(cst.getValue()));

                valueStack.push(new ConstantValue(WeacType.BOOLEAN_TYPE));
                currentVarType = WeacType.BOOLEAN_TYPE;
                staticness.setCurrent(false).push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_STRING_CONSTANT) {
                LoadStringConstant cst = ((LoadStringConstant) precompiledInsn);
                insns.add(stringResolver.resolve(cst.getValue()));
                valueStack.push(new ConstantValue(WeacType.STRING_TYPE));

                currentVarType = WeacType.STRING_TYPE;
                staticness.setCurrent(false).push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_CHARACTER_CONSTANT) {
                LoadCharacterConstant cst = ((LoadCharacterConstant) precompiledInsn);
                insns.add(new LoadCharInsn(stringResolver.resolveSingleCharacter(cst.getValue().toCharArray(), 0)));
                valueStack.push(new ConstantValue(WeacType.CHAR_TYPE));

                currentVarType = WeacType.CHAR_TYPE;
                staticness.setCurrent(false).push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.ARGUMENT_SEPARATOR) {
                currentVarType = selfType;

                staticness.pop();
                staticness.setCurrent(true).push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.DUP) {
                insns.add(new ResolvedInsn(ResolveOpcodes.DUP));
                Value val = valueStack.pop();
                valueStack.push(val);
                valueStack.push(val);

                boolean staticnessVal = staticness.pop();
                staticness.setCurrent(staticnessVal).push().push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_VARIABLE) {
                LoadVariable ldVar = ((LoadVariable) precompiledInsn);
                String varName = ldVar.getName();
                if(varName.equals("this")) {
                    insns.add(new LoadThisInsn(selfType));
                    valueStack.push(new ThisValue(selfType));
                    currentVarType = selfType;
                    staticness.setCurrent(false).push();
                } else {
                    // check if local variable
                    if(varMap.localExists(varName)) {
                        int index = varMap.getLocalIndex(varName);
                        WeacType type = varMap.getLocalType(varName);
                        insns.add(new LoadVariableInsn(index, type));
                        WeacType localType = varMap.getLocalType(varName);
                        valueStack.push(new VariableValue(varName, localType, index));
                        currentVarType = localType;
                        staticness.setCurrent(false).push();
                    } else {
                        // check if field
                        if(!variableMaps.containsKey(currentVarType)) {
                            PrecompiledClass clazz = findClass(currentVarType.getIdentifier().getId(), context);
                            registerVariables(clazz, variableMaps, context);
                        }
                        if(variableMaps.get(currentVarType).fieldExists(varName)) {
                            if(currentVarType.equals(selfType)) {
                                ResolvedInsn previous = insns.get(insns.size() - 1);
                                if(!(previous instanceof LoadThisInsn)) {
                                    insns.add(new LoadThisInsn(selfType));
                                    valueStack.push(new ThisValue(selfType));
                                    currentVarType = selfType;
                                    staticness.setCurrent(false).push();
                                }
                            }
                            System.out.println("PL FOUND in "+currentVarType.getIdentifier()+" : "+varName);
                            WeacType fieldType = variableMaps.get(currentVarType).getFieldType(varName);
                            boolean isStatic = staticness.pop();
                            insns.add(new LoadFieldInsn(varName, currentVarType, fieldType, isStatic));
                            valueStack.pop();
                            System.out.println("2PL FOUND in "+currentVarType.getIdentifier()+" : "+varName);
                            valueStack.push(new FieldValue(varName, currentVarType, fieldType));
                            currentVarType = fieldType;
                            staticness.setCurrent(false).push();
                        } else {
                            System.out.println("Did not find field "+varName+" in "+currentVarType+" (self is "+selfType+")");
                            PrecompiledClass clazz = findClass(varName, context);
                            if(clazz != null) {
                                currentVarType = resolveType(new Identifier(clazz.fullName, true), context);
                                if(clazz instanceof JavaImportedClass) {
                                    staticness.setCurrent(true).push();
                                    valueStack.push(new ConstantValue(currentVarType));
                                    System.out.println("FROM JAVA!! "+currentVarType);
                                } else if(clazz.classType == EnumClassTypes.OBJECT) {
                                    insns.add(new LoadFieldInsn(Constants.SINGLETON_INSTANCE_FIELD, currentVarType, currentVarType, true));
                                    valueStack.push(new FieldValue(Constants.SINGLETON_INSTANCE_FIELD, currentVarType, currentVarType));
                                    staticness.setCurrent(false).push();
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
                FunctionCall cst = ((FunctionCall) precompiledInsn);
                Value owner;
                String name;
                if(cst.getArgCount() != 0)
                    staticness.pop(); // remove value on top of stack
                boolean isStatic = staticness.pop();
                if(cst.getName().equals("this")) { // call constructor
                    owner = new ThisValue(selfType);
                    name = selfType.getIdentifier().getId();
                } else if(cst.getName().equals("super")) { // call super constructor
                    owner = new ThisValue(selfType.getSuperType());
                    name = selfType.getSuperType().getIdentifier().getId();
                } else {
                    owner = findFunctionOwner(valueStack, selfType, cst, context, isStatic);
                    name = cst.getName();
                }

                WeacType[] argTypes = new WeacType[cst.getArgCount()];

                PrecompiledMethod realMethod = findMethod(owner.getType(), name, cst.getArgCount(), valueStack, context);

                if(realMethod == null) {
                    newError("Could not find method named "+name+" in "+owner.getType()+" with "+cst.getArgCount()+" argument(s). (current class: "+selfType+")", -1);
                }

                for(int i0 = 0;i0<argTypes.length;i0++) {
                    argTypes[i0] = resolveType(realMethod.argumentTypes.get(i0), context);
                }

                WeacType returnType = resolveType(realMethod.returnType, context);
                insns.add(new FunctionCallInsn(name, owner.getType(), cst.getArgCount(), cst.shouldLookForInstance() && !realMethod.isJavaImported, argTypes, returnType, isStatic));

                staticness.setCurrent(false).push();
                int toPop = cst.getArgCount();
                if(valueStack.size() < toPop) {
                    throw new RuntimeException(name+" / "+toPop+" "+Arrays.toString(valueStack.toArray())+" / "+cst.shouldLookForInstance()+" (java: "+realMethod.isJavaImported+")");
                }
                for(int _dontcare = 0;_dontcare<toPop;_dontcare++) {
                    valueStack.pop();
                }
                if(cst.shouldLookForInstance())
                    valueStack.pop();
                if(!returnType.equals(WeacType.VOID_TYPE))
                    valueStack.push(new ConstantValue(returnType));
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.RETURN) {
                if(valueStack.isEmpty()) {
                    insns.add(new ResolvedInsn(ResolveOpcodes.RETURN));
                } else {
                    Value val = valueStack.pop();
                    WeacType type = val.getType();
                    int opcode = ResolveOpcodes.OBJ_RETURN;
                    if(type == null) {
                        opcode = ResolveOpcodes.RETURN;
                    } else if(type.equals(WeacType.BOOLEAN_TYPE)) {
                        opcode = ResolveOpcodes.BOOL_RETURN;
                    } else if(type.equals(WeacType.BYTE_TYPE)) {
                        opcode = ResolveOpcodes.BYTE_RETURN;
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

                    insns.add(new ResolvedInsn(opcode));
                }
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.THIS) {
                insns.add(new LoadThisInsn(selfType));
                valueStack.push(new ThisValue(selfType));
                staticness.setCurrent(false).push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_NULL) {
                insns.add(new LoadNullInsn());
                valueStack.push(new NullValue());
                staticness.setCurrent(false).push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LABEL) {
                while(!valueStack.isEmpty()) {
                    if(valueStack.peek().getType().equals(selfType)) {
                        break;
                    }
                    Value val = valueStack.pop();
                    if(selfType.getIdentifier().getId().endsWith("Console")) {
                        System.out.println("POP:"+val);
                    }
                    insns.add(new PopInsn());
                }
                LabelInsn cst = ((LabelInsn) precompiledInsn);
                insns.add(new ResolvedLabelInsn(cst.getLabel()));
                currentVarType = selfType;
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.BINARY_OPERATOR) {
                OperatorInsn insn = ((OperatorInsn) precompiledInsn);
                EnumOperators op = insn.getOperator();
                if(op.isVariableAssign()) {
                    switch(op) {
                        case SET_TO: {
                            Value toAssign = valueStack.pop();
                            staticness.pop();
                            Value potentialVariable = valueStack.pop();
                            staticness.pop();
                            if (potentialVariable.isConstant()) {
                                newError(potentialVariable.getName() + "Invalid assigment, can only assign a value to a field or a variable", -1); // todo line
                            } else {
                                String name = potentialVariable.getName();
                                if (potentialVariable instanceof VariableValue) { // local variables take priority
                                    for(int i0 = insns.size()-1;i0>=0;i0--) {
                                        ResolvedInsn in = insns.get(i0);
                                        if(in instanceof LoadVariableInsn) {
                                            LoadVariableInsn variableInsn = (LoadVariableInsn) in;
                                            int index = variableInsn.getVarIndex();
                                            if(varMap.getLocalName(index).equals(potentialVariable.getName())) {
                                                insns.remove(i0);
                                                break;
                                            }
                                        }
                                    }
                                    insns.add(new StoreVarInsn(varMap.getLocalIndex(name)));
                                } else if (potentialVariable instanceof FieldValue) {
                                    FieldValue fieldValue = (FieldValue) potentialVariable;
                                    for(int i0 = insns.size()-1;i0>=0;i0--) {
                                        ResolvedInsn in = insns.get(i0);
                                        if(in instanceof LoadFieldInsn) {
                                            LoadFieldInsn fieldInsn = (LoadFieldInsn) in;
                                            if(fieldInsn.getOwner().equals(fieldValue.getOwner()) && fieldInsn.getFieldName().equals(fieldValue.getName())) {
                                                insns.remove(i0);
                                                break;
                                            }
                                        }
                                    }
                                    if (currentVarType.equals(selfType)) {
                                        ResolvedInsn previous = insns.get(insns.size() - 1);
                                        if (!(previous instanceof LoadThisInsn)) {
                                            insns.add(new LoadThisInsn(selfType));
                                        }
                                    }

                                    WeacType fieldType = fieldValue.getType();
                                    insns.add(new StoreFieldInsn(name, fieldValue.getOwner(), fieldType, staticness.pop()));
                                    currentVarType = fieldType;
                                    staticness.setCurrent(false).push();
                                } else {
                                    newError("local or field " + name + " does not exist ("+currentVarType+")", -1); // todo line
                                }
                            }
                        }
                        break;

                        default:
                            System.err.println("UNRESOLVED OP: " + op.name());
                            break;
                    }
                } else {
                    switch (op) {
                        case EQUAL:
                            Value second = valueStack.pop();
                            staticness.pop();
                            Value first = valueStack.pop();
                            staticness.pop();
                            if(second.getType().isPrimitive() && first.getType().isPrimitive()) {
                                insns.add(new SubtractInsn(first.getType()));
                                insns.add(new CheckNotZero());
                            } else if(first.getType().isPrimitive() && !second.getType().isPrimitive()) {
                                // TODO
                                newError("Dunno what to do", -1);
                            } else {
                                insns.add(new ObjectEqualInsn());
                            }
                            valueStack.push(new ConstantValue(WeacType.BOOLEAN_TYPE));
                            staticness.setCurrent(false).push();
                            break;

                        case MULTIPLY: {
                            valueStack.pop();
                            Value last = valueStack.pop();
                            valueStack.push(new ConstantValue(last.getType()));
                            staticness.pop();
                            staticness.pop();
                            staticness.setCurrent(false).push();

                            insns.add(new MultiplyInsn(last.getType()));
                        }
                        break;

                        case MINUS: {
                            valueStack.pop();
                            Value last = valueStack.pop();
                            valueStack.push(new ConstantValue(last.getType()));
                            staticness.pop();
                            staticness.pop();
                            staticness.setCurrent(false).push();

                            insns.add(new SubtractInsn(last.getType()));
                        }
                        break;

                        case MOD: {
                            valueStack.pop();
                            Value last = valueStack.pop();
                            valueStack.push(new ConstantValue(last.getType()));
                            staticness.pop();
                            staticness.pop();
                            staticness.setCurrent(false).push();

                            insns.add(new ModuloInsn(last.getType()));
                        }
                        break;

                        default:
                            System.err.println("UNRESOLVED OP: " + op.name());
                            break;
                    }
                }
                // TODO
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.NEW_LOCAL) {
                NewLocalVar localVar = ((NewLocalVar) precompiledInsn);
                WeacType type = resolveType(new Identifier(localVar.getType(), true), context);
                varMap.registerLocal(localVar.getName(), type);
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.NEW) {
                InstanciateInsn instanciateInsn = (InstanciateInsn) precompiledInsn;
                WeacType type = resolveType(new Identifier(instanciateInsn.getTypeName(), false), context);
                insns.add(new NewInsn(type));
                valueStack.push(new ConstantValue(type));
                staticness.setCurrent(false).push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.THROW) {
                Value exception = valueStack.pop();
                valueStack.clear();
                //valueStack.push(exception);
                insns.add(new ResolvedInsn(ResolveOpcodes.THROW));
            } else if(precompiledInsn instanceof IfNotJumpInsn) {
                valueStack.pop();
                staticness.pop();
                insns.add(new IfNotJumpResInsn(((IfNotJumpInsn) precompiledInsn).getJumpTo()));
            } else if(precompiledInsn instanceof GotoInsn) {
                insns.add(new GotoResInsn(((GotoInsn) precompiledInsn).getLabel()));
            } else {
                System.err.println("UNRESOLVED: "+precompiledInsn);
            }
        }
        return insns;
    }

    private void registerVariables(PrecompiledClass cl, Map<WeacType, VariableMap> variableMaps, ResolvingContext context) {
        WeacType type = resolveType(new Identifier(cl.fullName, true), context);
        VariableMap map = new VariableMap();
        for(PrecompiledField f : cl.fields) {
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

    private PrecompiledMethod findMethod(WeacType topType, String name, int argCount, Stack<Value> valueStack, ResolvingContext context) {
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

    private PrecompiledMethod findMethodFromHierarchy(WeacType topType, String name, int argCount, WeacType[] argTypes, ResolvingContext context) {
        PrecompiledClass topClass = findClass(topType.getIdentifier().getId(), context);
        Optional<PrecompiledMethod> foundMethod = topClass.methods.stream()
                .filter(m -> m.name.getId().equals(name) || (m.isConstructor && topType.getIdentifier().getId().endsWith(name)))
                .filter(m -> m.argumentNames.size() == argCount)
                .filter(m -> {
                    for(int i = 0;i<argTypes.length;i++) {
                        WeacType argType = argTypes[i];
                        WeacType paramType = resolveType(m.argumentTypes.get(i), context);
                        if(!isCastable(argType, paramType, context)) {
                            return false;
                        }
                    }
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

        if(topClass.fullName.equals(Object.class.getCanonicalName())) {
            return null; // We've reached the top, stop here
        }

        boolean isPresent = context.getSource().classes.stream().filter(c -> c.fullName.equals(topClass.fullName)).count() != 0L;
        ResolvingContext newContext;
        if(!isPresent) {
            PrecompiledSource newSource = new PrecompiledSource();
            newSource.classes = new LinkedList<>();
            newSource.classes.add(topClass);
            newSource.imports = topClass.imports;
            newSource.packageName = topClass.packageName;
            newContext = new ResolvingContext(newSource, context.getSideClasses());
        } else {
            newContext = context;
        }


        ClassHierarchy hierarchy = getHierarchy(topClass, topClass.interfacesImplemented, newContext);

        PrecompiledClass superclass = hierarchy.getSuperclass();
        if(superclass != null && !superclass.fullName.equals(topClass.fullName)) {
            WeacType superType = resolveType(new Identifier(superclass.fullName, true), context);
            PrecompiledMethod supermethod = findMethodFromHierarchy(superType, name, argCount, argTypes, context);
            if(supermethod == null) {
                // TODO: check interfaces
                List<PrecompiledClass> interfaces = hierarchy.getInterfaces();
                for(PrecompiledClass in : interfaces) {
                    WeacType interType = resolveType(new Identifier(in.fullName, true), context);
                    PrecompiledMethod m = findMethodFromHierarchy(interType, name, argCount, argTypes, context);
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

    private boolean isCastable(WeacType from, WeacType to, ResolvingContext context) {
        if(from.isArray() || to.isArray()) {
            return from.equals(to); // TODO: Implicit array casts?
        }
        if(from.equals(to)) {
            return true;
        }
        // TODO: primitive types
        if(primitiveCasts.isPrimitiveCast(from, to)) {
            return primitiveCasts.isCastable(from, to);
        }

        PrecompiledClass fromClass = findClass(from.getIdentifier().getId(), context);
        PrecompiledClass toClass = findClass(to.getIdentifier().getId(), context);
        return isCastable(fromClass, toClass, context);
    }

    private boolean isCastable(PrecompiledClass from, PrecompiledClass to, ResolvingContext context) {
        if(from.fullName.equals("java.lang.Object") && !to.fullName.equals("java.lang.Object"))
            return false;
        ClassHierarchy hierarchy = getHierarchy(from, from.interfacesImplemented, context);

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

    private WeacType extractType(ResolvedInsn number) {
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

    private Value findFunctionOwner(Stack<Value> stack, WeacType currentType, FunctionCall cst, ResolvingContext context, boolean isStatic) {
        if(cst.shouldLookForInstance()) {
            System.out.println("OWNER: "+currentType.getIdentifier()+" "+cst+" "+Arrays.toString(stack.toArray()));
            int args = cst.getArgCount();
            int index = stack.size()-args-1;
            return stack.get(index);
        } else {
            System.out.println("USED THIS: "+currentType.getIdentifier()+" "+cst+" "+Arrays.toString(stack.toArray()));
            return new ThisValue(currentType);
        }
    }

}
