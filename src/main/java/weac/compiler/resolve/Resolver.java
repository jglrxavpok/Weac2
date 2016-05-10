package weac.compiler.resolve;

import org.jglr.flows.collection.VariableTopStack;
import weac.compiler.CompileUtils;
import weac.compiler.parse.EnumClassTypes;
import weac.compiler.patterns.InstructionPattern;
import weac.compiler.precompile.Label;
import weac.compiler.precompile.insn.*;
import weac.compiler.precompile.structure.*;
import weac.compiler.resolve.insn.*;
import weac.compiler.resolve.structure.*;
import weac.compiler.resolve.values.*;
import weac.compiler.targets.WeacTarget;
import weac.compiler.targets.jvm.JVMConstants;
import weac.compiler.targets.jvm.JVMWeacTypes;
import weac.compiler.utils.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Resolver extends CompileUtils {

    private final NumberResolver numberResolver;
    private final StringResolver stringResolver;
    private final PrimitiveCastSolver primitiveCasts;
    private int labelID = -100;
    private AutoTypeResolver autoTypeResolver;
    private ExpressionResolver expressionResolver;
    private TypeResolver typeResolver;

    public Resolver(WeacTarget resolver) {
        this.typeResolver = resolver.newTypeResolver(this);
        expressionResolver = new ExpressionResolver(this);
        numberResolver = new NumberResolver();
        stringResolver = new StringResolver();
        primitiveCasts = new PrimitiveCastSolver();
        autoTypeResolver = new AutoTypeResolver();
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
        Stack<Value> valStack = new Stack<>();
        List<ResolvedInsn> insns = resolveSingleExpression(field.defaultValue, currentType, context, fieldVarMap, valStack);
        if(!field.defaultValue.isEmpty()) {
            if(valStack.size() != 1) {
                newError("Invalid default value for field "+field.name, -1);
            } else {
                if(resolvedField.type.equals(WeacType.AUTO)) {
                    resolvedField.type = autoTypeResolver.findEndType(insns);
                }
                Value result = valStack.pop();
                if(!result.getType().equals(resolvedField.type)) {
                    insns.add(new CastInsn(result.getType(), resolvedField.type));
                }
            }
        }

        if(resolvedField.type.equals(WeacType.AUTO) && field.defaultValue.isEmpty()) {
            newError("Cannot infer type from unitialized field", -1); // todo line
        }
        resolvedField.defaultValue.addAll(insns);
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
        Stack<Value> valStack = new Stack<>();
        resolveSingleExpression(precompiledMethod.instructions, currentType, context, localVariables, valStack).forEach(method.instructions::add);
        return method;
    }

    private void registerFields(VariableMap variables, PrecompiledClass precompiledClass, ResolvingContext context) {
        for(PrecompiledField f : precompiledClass.fields) {
            WeacType type = resolveType(f.type, context);
            String name = f.name.getId();
            variables.registerField(name, type);
        }
    }

    protected WeacType resolveType(Identifier type, ResolvingContext context) {
        return typeResolver.resolveType(type, context);
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

    public ClassHierarchy getHierarchy(PrecompiledClass aClass, List<String> interfacesImplemented, ResolvingContext context) {
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
            superclass = findClass(JVMConstants.BASE_CLASS, context);
        }
        parents.setSuperclass(superclass);
        return parents;
    }

    public PrecompiledClass findClass(String inter, ResolvingContext context) {
        return typeResolver.findClass(inter, context);
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
            resolved.ordinal = cst.ordinal;

            Stack<Value> valueStack = new Stack<>();
            if(cst.parameters != null) {
                cst.parameters.stream()
                        .map(m -> resolveSingleExpression(m, currentType, context, enumVarMap, valueStack))
                        .forEach(resolved.parameters::add);
            }
            PrecompiledMethod constructor = findMethod(currentType, "<init>", cst.parameters == null ? 0 : cst.parameters.size(), valueStack, context, enumVarMap);
            ConstructorInfos constructorInfos = new ConstructorInfos();
            constructorInfos.argNames.addAll(constructor.argumentNames);
            constructor.argumentTypes.stream()
                    .map(t -> resolveType(t, context))
                    .forEach(constructorInfos.argTypes::add);
            resolved.usedConstructor = constructorInfos;

            resolvedConstants.add(resolved);
        }
        return resolvedConstants;
    }

    private List<ResolvedInsn> resolveSingleExpression(List<PrecompiledInsn> precompiled, WeacType selfType, ResolvingContext context, VariableMap varMap) {
        return resolveSingleExpression(precompiled, selfType, context, varMap, new Stack<>());
    }

    private List<ResolvedInsn> resolveSingleExpression(List<PrecompiledInsn> precompiled, WeacType selfType, ResolvingContext context, VariableMap varMap, Stack<Value> valueStack) {
        return expressionResolver.resolve(precompiled, selfType, context, varMap, valueStack);
    }

    protected Label newLabel() {
        return new Label(labelID--);
    }

    protected WeacType findResultType(WeacType left, WeacType right, ResolvingContext context) {
        if(left.equals(right)) {
            return left;
        } else if(isCastable(left, right, context)) {
            return right;
        } else if(isCastable(right, left, context)) {
            return left;
        }
        return JVMWeacTypes.JOBJECT_TYPE;
    }

    protected void registerVariables(PrecompiledClass cl, Map<WeacType, VariableMap> variableMaps, ResolvingContext context) {
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

    protected PrecompiledMethod findMethod(WeacType topType, String name, int argCount, Stack<Value> valueStack, ResolvingContext context, VariableMap varMap) {
        WeacType[] potentialArgTypes = new WeacType[argCount];
        for(int i = 0;i<argCount;i++) {
            int index = valueStack.size()-(argCount-i-1)-1;
            potentialArgTypes[i] = valueStack.get(index).getType();
        }
        return findMethodFromHierarchy(topType, name, argCount, potentialArgTypes, context, varMap);
    }

    private PrecompiledMethod findMethodFromHierarchy(WeacType topType, String name, int argCount, WeacType[] argTypes, ResolvingContext context, VariableMap varMap) {
        PrecompiledClass topClass = findClass(topType.getIdentifier().getId(), context);
        List<PrecompiledMethod> result = topClass.methods.stream()
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
                    int aMethodScore = computeMethodScore(a, argTypes, context);
                    int bMethodScore = computeMethodScore(b, argTypes, context);
                    a.score = aMethodScore;
                    b.score = bMethodScore;
                    return Integer.compare(aMethodScore, bMethodScore); // the lower the better
                })
                .collect(Collectors.toList());
        if(result.size() > 0) {
            if(result.size() > 1) {
                PrecompiledMethod first = result.get(0);
                PrecompiledMethod second = result.get(1);
                if(first.score == second.score) {
                    newError("Ambiguous method call ("+name+"): "+Arrays.toString(first.argumentTypes.toArray())+" or "+Arrays.toString(second.argumentTypes.toArray())+" for arguments "+Arrays.toString(argTypes)+"?", -1); // todo line
                }
            }
            return result.get(0);
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
            PrecompiledMethod supermethod = findMethodFromHierarchy(superType, name, argCount, argTypes, context, varMap);
            if(supermethod == null) {
                List<PrecompiledClass> interfaces = hierarchy.getInterfaces();
                for(PrecompiledClass in : interfaces) {
                    WeacType interType = resolveType(new Identifier(in.fullName, true), context);
                    PrecompiledMethod m = findMethodFromHierarchy(interType, name, argCount, argTypes, context, varMap);
                    if(m != null) {
                        return m;
                    }
                }

                if(superType.equals(JVMWeacTypes.OBJECT_TYPE) || superType.equals(JVMWeacTypes.JOBJECT_TYPE)) {
                    return null;
                }
            } else {
                return supermethod;
            }
        }

        return null;
    }

    private int computeMethodScore(PrecompiledMethod method, WeacType[] givenTypes, ResolvingContext context) {
        int totalScore = 0;
        for (int i = 0; i < givenTypes.length; i++) {
            WeacType requiredType = resolveType(method.argumentTypes.get(i), context);
            totalScore += computeScore(requiredType, givenTypes[i]);
        }
        return totalScore;
    }

    /**
     * Method call solving involves a score-system. For each argument, a score will be computed, it corresponds to
     * the number of classes in the hierarchy separating the given argument type and the required one.
     * <code>null</code> corresponds to a score of 0.
     * @param requiredType
     * @param givenType
     * @return
     */
    private int computeScore(WeacType requiredType, WeacType givenType) {
        if(givenType.equals(JVMWeacTypes.NULL_TYPE) || requiredType.equals(givenType))
            return 0;
        if(primitiveCasts.isPrimitiveCast(givenType, requiredType))
            if(primitiveCasts.isCastable(givenType, requiredType)) {
                return primitiveCasts.size(requiredType) - primitiveCasts.size(givenType);
            }
        WeacType superType = givenType.getSuperType();
        if(superType != null && superType != JVMWeacTypes.JOBJECT_TYPE) {
            return 1+computeScore(requiredType, superType);
        }
        return 10;
    }

    protected boolean isCastable(WeacType from, WeacType to, ResolvingContext context) {
        if(from.isArray() || to.isArray()) {
            return from.equals(to); // TODO: Implicit array casts?
        }
        if(from.equals(to)) {
            return true;
        }
        if(primitiveCasts.isPrimitiveCast(from, to)) {
            return primitiveCasts.isCastable(from, to);
        }

        PrecompiledClass fromClass = findClass(from.getIdentifier().getId(), context);
        PrecompiledClass toClass = findClass(to.getIdentifier().getId(), context);
        return isCastable(fromClass, toClass, context);
    }

    private boolean isCastable(PrecompiledClass from, PrecompiledClass to, ResolvingContext context) {
        return typeResolver.isCastable(from, to, context);
    }

    protected Value findFunctionOwner(Stack<Value> stack, WeacType currentType, FunctionCall cst, ResolvingContext context, boolean isStatic) {
        if(cst.shouldLookForInstance()) {
            int args = cst.getArgCount();
            int index = stack.size()-args-1;
            return stack.get(index);
        } else {
            return new ThisValue(currentType);
        }
    }

    public NumberResolver getNumberResolver() {
        return numberResolver;
    }

    public StringResolver getStringResolver() {
        return stringResolver;
    }
}
