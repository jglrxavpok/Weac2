package weac.compiler.resolve;

import org.jglr.flows.collection.VariableTopStack;
import weac.compiler.CompileUtils;
import weac.compiler.parse.EnumClassTypes;
import weac.compiler.precompile.Label;
import weac.compiler.precompile.insn.*;
import weac.compiler.precompile.structure.JavaImportedClass;
import weac.compiler.precompile.structure.PrecompiledClass;
import weac.compiler.precompile.structure.PrecompiledMethod;
import weac.compiler.resolve.insn.*;
import weac.compiler.resolve.values.*;
import weac.compiler.targets.jvm.JVMWeacTypes;
import weac.compiler.utils.Constants;
import weac.compiler.utils.EnumOperators;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.WeacType;

import java.util.*;
import java.util.function.Function;

public class ExpressionResolver extends CompileUtils {

    private final Resolver resolver;
    private final Map<EnumOperators, Function<WeacType, OperationInsn>> operatorsInsnFactories;

    public ExpressionResolver(Resolver resolver) {
        this.resolver = resolver;
        operatorsInsnFactories = new HashMap<>();
        operatorsInsnFactories.put(EnumOperators.MINUS, SubtractInsn::new);
        operatorsInsnFactories.put(EnumOperators.PLUS, AddInsn::new);
        operatorsInsnFactories.put(EnumOperators.MOD, ModulusInsn::new);
        operatorsInsnFactories.put(EnumOperators.MULTIPLY, MultiplyInsn::new);
        operatorsInsnFactories.put(EnumOperators.DIVIDE, DivideInsn::new);

        operatorsInsnFactories.put(EnumOperators.LESS_OR_EQUAL, LessOrEqualInsn::new);
        operatorsInsnFactories.put(EnumOperators.LESS_THAN, LessInsn::new);

        operatorsInsnFactories.put(EnumOperators.GREATER_OR_EQUAL, GreaterOrEqualInsn::new);
        operatorsInsnFactories.put(EnumOperators.GREATER_THAN, GreaterInsn::new);
    }

    public List<ResolvedInsn> resolve(List<PrecompiledInsn> precompiled, WeacType selfType, ResolvingContext context, VariableMap varMap, Stack<Value> valueStack) {
        List<ResolvedInsn> insns = new LinkedList<>();
        VariableTopStack<Boolean> staticness = new VariableTopStack<>();
        staticness.setCurrent(false).push();

        Map<WeacType, VariableMap> variableMaps = new HashMap<>();
        // fill variableMaps
        for(PrecompiledClass cl : context.getSideClasses()) {
            resolver.registerVariables(cl, variableMaps, context);
        }
        variableMaps.put(selfType, varMap);

        WeacType currentVarType = selfType;
        for(int i = 0;i<precompiled.size();i++) {
            PrecompiledInsn precompiledInsn = precompiled.get(i);
            if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_NUMBER_CONSTANT) {
                LoadNumberConstant cst = ((LoadNumberConstant) precompiledInsn);
                String numberRepresentation = cst.getValue();
                ResolvedInsn resolvedNumber = resolver.getNumberResolver().resolve(numberRepresentation);

                WeacType numberType = extractType(resolvedNumber);

                valueStack.push(new ConstantValue(numberType));

                insns.add(resolvedNumber);

                currentVarType = numberType;
                staticness.setCurrent(false).push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.FUNCTION_START) {
                FunctionStartInsn startInsn = (FunctionStartInsn)precompiledInsn;
                WeacType owner = valueStack.isEmpty() ? selfType : valueStack.peek().getType();
                if(!startInsn.shouldLookForInstance()) {
                    insns.add(new LoadThisInsn(selfType));
                }
                insns.add(new FunctionStartResInsn(startInsn.getFunctionName(), startInsn.getArgCount(), owner));
                currentVarType = selfType;
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_BOOLEAN_CONSTANT) {
                LoadBooleanConstant cst = ((LoadBooleanConstant) precompiledInsn);
                insns.add(new LoadBooleanInsn(cst.getValue()));

                valueStack.push(new ConstantValue(JVMWeacTypes.BOOLEAN_TYPE));
                currentVarType = JVMWeacTypes.BOOLEAN_TYPE;
                staticness.setCurrent(false).push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_STRING_CONSTANT) {
                LoadStringConstant cst = ((LoadStringConstant) precompiledInsn);
                insns.add(resolver.getStringResolver().resolve(cst.getValue()));
                valueStack.push(new ConstantValue(JVMWeacTypes.STRING_TYPE));

                currentVarType = JVMWeacTypes.STRING_TYPE;
                staticness.setCurrent(false).push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.LOAD_CHARACTER_CONSTANT) {
                LoadCharacterConstant cst = ((LoadCharacterConstant) precompiledInsn);
                insns.add(new LoadCharInsn(resolver.getStringResolver().resolveSingleCharacter(cst.getValue().toCharArray(), 0)));
                valueStack.push(new ConstantValue(JVMWeacTypes.CHAR_TYPE));

                currentVarType = JVMWeacTypes.CHAR_TYPE;
                staticness.setCurrent(false).push();
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.ARGUMENT_SEPARATOR) {
                currentVarType = selfType;

                staticness.pop();
                staticness.setCurrent(false).push();
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
                            PrecompiledClass clazz = resolver.findClass(currentVarType.getIdentifier().getId(), context);
                            resolver.registerVariables(clazz, variableMaps, context);
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
                            WeacType fieldType = variableMaps.get(currentVarType).getFieldType(varName);
                            boolean isStatic = staticness.pop();
                            insns.add(new LoadFieldInsn(varName, currentVarType, fieldType, isStatic));
                            valueStack.pop();
                            valueStack.push(new FieldValue(varName, currentVarType, fieldType));
                            currentVarType = fieldType;
                            staticness.setCurrent(false).push();
                        } else {
                            PrecompiledClass clazz = resolver.findClass(varName, context);
                            if(clazz != null) {
                                currentVarType = resolver.resolveType(new Identifier(clazz.fullName, true), context);
                                if(clazz instanceof JavaImportedClass) {
                                    staticness.setCurrent(true).push();
                                    valueStack.push(new ConstantValue(currentVarType));
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
                    owner = resolver.findFunctionOwner(valueStack, selfType, cst, context, isStatic);
                    name = cst.getName();
                }

                WeacType[] argTypes = new WeacType[cst.getArgCount()];

                PrecompiledMethod realMethod = resolver.findMethod(owner.getType(), name, cst.getArgCount(), valueStack, context, varMap);

                if(realMethod == null) {
                    newError("Could not find method named "+name+" in "+owner.getType()+" with "+cst.getArgCount()+" argument(s). (current class: "+selfType+")", -1);
                }

                for(int i0 = 0;i0<argTypes.length;i0++) {
                    argTypes[i0] = resolver.resolveType(realMethod.argumentTypes.get(i0), context);
                }

                WeacType returnType = resolver.resolveType(realMethod.returnType, context);
                insns.add(new FunctionCallInsn(realMethod.name.getId(), owner.getType(), cst.getArgCount(), argTypes, realMethod.isConstructor ? JVMWeacTypes.VOID_TYPE : returnType, isStatic));

                staticness.setCurrent(false).push();
                int toPop = cst.getArgCount();
                if(valueStack.size() < toPop) {
                    throw new RuntimeException(name+" / "+toPop+" "+ Arrays.toString(valueStack.toArray())+" / "+cst.shouldLookForInstance()+" (java: "+realMethod.isJavaImported+")");
                }
                for(int _dontcare = 0;_dontcare<toPop;_dontcare++) {
                    valueStack.pop();
                }
                if(cst.shouldLookForInstance())
                    valueStack.pop();
                if(!returnType.equals(JVMWeacTypes.VOID_TYPE))
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
                    } else if(type.equals(JVMWeacTypes.BOOLEAN_TYPE)) {
                        opcode = ResolveOpcodes.BOOL_RETURN;
                    } else if(type.equals(JVMWeacTypes.BYTE_TYPE)) {
                        opcode = ResolveOpcodes.BYTE_RETURN;
                    } else if(type.equals(JVMWeacTypes.CHAR_TYPE)) {
                        opcode = ResolveOpcodes.CHAR_RETURN;
                    } else if(type.equals(JVMWeacTypes.DOUBLE_TYPE)) {
                        opcode = ResolveOpcodes.DOUBLE_RETURN;
                    } else if(type.equals(JVMWeacTypes.FLOAT_TYPE)) {
                        opcode = ResolveOpcodes.FLOAT_RETURN;
                    } else if(type.equals(JVMWeacTypes.INTEGER_TYPE)) {
                        opcode = ResolveOpcodes.INT_RETURN;
                    } else if(type.equals(JVMWeacTypes.LONG_TYPE)) {
                        opcode = ResolveOpcodes.LONG_RETURN;
                    } else if(type.equals(JVMWeacTypes.SHORT_TYPE)) {
                        opcode = ResolveOpcodes.SHORT_RETURN;
                    } else if(type.equals(JVMWeacTypes.VOID_TYPE)) {
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
                    insns.add(new PopInsn(val.getType()));
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
                                WeacType varType = toAssign.getType();
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
                                    WeacType type = varMap.getLocalType(name);

                                    if(!type.equals(varType)) {
                                        insns.add(new CastInsn(varType, type));
                                    }
                                    insns.add(new StoreVarInsn(varMap.getLocalIndex(name), varType));
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

                                    WeacType type = varMap.getFieldType(name);

                                    if(!type.equals(varType)) {
                                        insns.add(new CastInsn(varType, type));
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
                        case EQUAL: {
                            Value second = valueStack.pop();
                            staticness.pop();
                            Value first = valueStack.pop();
                            staticness.pop();

                            WeacType resultType = resolver.findResultType(first.getType(), second.getType(), context);

                            if (!first.getType().equals(resultType)) {
                                int tmpVarIndex = varMap.registerLocal("$temp" + varMap.getCurrentLocalIndex(), second.getType());
                                insns.add(new StoreVarInsn(tmpVarIndex, second.getType()));

                                insns.add(new CastInsn(first.getType(), resultType));
                                insns.add(new LoadVariableInsn(tmpVarIndex, second.getType()));
                            }

                            if (!second.getType().equals(resultType)) {
                                insns.add(new CastInsn(second.getType(), resultType));
                            }

                            if (second.getType().isPrimitive() && first.getType().isPrimitive()) {
                                insns.add(new SubtractInsn(resultType));
                                if (!resolver.isCastable(resultType, JVMWeacTypes.INTEGER_TYPE, context)) {
                                    insns.add(new CompareInsn(resultType));
                                }
                                insns.add(new CheckZero());
                            } else if (first.getType().isPrimitive() && !second.getType().isPrimitive()
                                    || !first.getType().isPrimitive() && second.getType().isPrimitive()) {
                                // TODO
                                newError("Dunno what to do, == operation between "+first+" and "+second+" in "+context.getSource().classes.get(0).fullName, -1);
                            } else {
                                insns.add(new ObjectEqualInsn());
                            }
                            valueStack.push(new ConstantValue(JVMWeacTypes.BOOLEAN_TYPE));
                            staticness.setCurrent(false).push();
                        }
                        break;

                        case LESS_THAN:
                        case GREATER_THAN:
                        case LESS_OR_EQUAL:
                        case GREATER_OR_EQUAL: {
                            Value right = valueStack.pop();
                            Value left = valueStack.pop();
                            WeacType resultType = resolver.findResultType(left.getType(), right.getType(), context);

                            insns.add(new SubtractInsn(resultType));

                            if (!left.getType().equals(resultType)) {
                                int tmpVarIndex = varMap.registerLocal("$temp" + varMap.getCurrentLocalIndex(), right.getType());
                                insns.add(new StoreVarInsn(tmpVarIndex, right.getType()));

                                insns.add(new CastInsn(left.getType(), resultType));
                                insns.add(new LoadVariableInsn(tmpVarIndex, right.getType()));
                            }

                            if (!right.getType().equals(resultType)) {
                                insns.add(new CastInsn(right.getType(), resultType));
                            }

                            valueStack.push(new ConstantValue(JVMWeacTypes.BOOLEAN_TYPE));
                            staticness.pop();
                            staticness.pop();
                            staticness.setCurrent(false).push();

                            if(!resultType.equals(JVMWeacTypes.INTEGER_TYPE)) {
                                insns.add(new CompareInsn(resultType));
                            }

                            insns.add(createOperatorInsn(resultType, op));
                        }
                        break;

                        case MINUS:
                        case MOD:
                        case PLUS:
                        case DIVIDE:
                        case MULTIPLY: {
                            Value right = valueStack.pop();
                            Value left = valueStack.pop();
                            WeacType resultType = resolver.findResultType(left.getType(), right.getType(), context);

                            if(!left.getType().equals(resultType)) {
                                int tmpVarIndex = varMap.registerLocal("$temp"+varMap.getCurrentLocalIndex(), right.getType());
                                insns.add(new StoreVarInsn(tmpVarIndex, right.getType()));

                                insns.add(new CastInsn(left.getType(), resultType));
                                insns.add(new LoadVariableInsn(tmpVarIndex, right.getType()));
                            }

                            if(!right.getType().equals(resultType)) {
                                insns.add(new CastInsn(right.getType(), resultType));
                            }

                            valueStack.push(new ConstantValue(resultType));
                            staticness.pop();
                            staticness.pop();
                            staticness.setCurrent(false).push();

                            insns.add(createOperatorInsn(resultType, op));
                        }
                        break;

                        case DOUBLE_OR:
                        case DOUBLE_AND: {
                            // TODO: Probably will be up to the precompiler to actually support those and convert them to & and |
                            throw new UnsupportedOperationException("&& and || are not supported yet, please use & or | for the moment");
                        }

                        case AND: {
                            Value right = valueStack.pop();
                            Value left = valueStack.pop();
                            if(!left.getType().equals(JVMWeacTypes.BOOLEAN_TYPE) || !right.getType().equals(JVMWeacTypes.BOOLEAN_TYPE)) {
                                newError("Cannot perform AND operation "+left.getType()+" & "+right.getType(), -1); // todo line
                            }
                            Label labelFalse1 = resolver.newLabel();
                            Label labelFalse0 = resolver.newLabel();
                            Label labelTrue = resolver.newLabel();
                            Label endLabel = resolver.newLabel();
                            insns.add(new IfNotJumpResInsn(labelFalse1));
                            insns.add(new IfNotJumpResInsn(labelFalse0));
                            insns.add(new GotoResInsn(labelTrue));

                            insns.add(new ResolvedLabelInsn(labelFalse1));
                            insns.add(new PopInsn(JVMWeacTypes.BOOLEAN_TYPE));
                            insns.add(new ResolvedLabelInsn(labelFalse0));
                            insns.add(new LoadBooleanInsn(false));
                            insns.add(new GotoResInsn(endLabel));
                            insns.add(new ResolvedLabelInsn(labelTrue));
                            insns.add(new LoadBooleanInsn(true));

                            insns.add(new ResolvedLabelInsn(endLabel));

                            valueStack.push(new ConstantValue(JVMWeacTypes.BOOLEAN_TYPE));
                            staticness.pop();
                            staticness.pop();
                            staticness.setCurrent(false).push();
                        }
                        break;

                        case INTERVAL_SEPARATOR: {
                            Value upperBound = valueStack.pop();
                            Value lowerBound = valueStack.pop();
                            if(!lowerBound.getType().equals(JVMWeacTypes.DOUBLE_TYPE)) {
                                int tmpVarIndex = varMap.registerLocal("$temp"+varMap.getCurrentLocalIndex(), upperBound.getType());
                                insns.add(new StoreVarInsn(tmpVarIndex, upperBound.getType()));

                                insns.add(new CastInsn(lowerBound.getType(), JVMWeacTypes.DOUBLE_TYPE));
                                insns.add(new LoadVariableInsn(tmpVarIndex, upperBound.getType()));
                            }

                            if(!upperBound.getType().equals(JVMWeacTypes.DOUBLE_TYPE)) {
                                insns.add(new CastInsn(upperBound.getType(), JVMWeacTypes.DOUBLE_TYPE));
                            }

                            int startIndex = findPreviousArrayStart(i, precompiled);
                            insns.add(startIndex, new NewInsn(JVMWeacTypes.INTERVAL_TYPE));
                            insns.add(startIndex+1, new ResolvedInsn(ResolveOpcodes.DUP));

                            insns.add(new FunctionStartResInsn("<init>", 2, JVMWeacTypes.INTERVAL_TYPE));
                            insns.add(new FunctionCallInsn("<init>", JVMWeacTypes.INTERVAL_TYPE, 2, new WeacType[]{JVMWeacTypes.DOUBLE_TYPE, JVMWeacTypes.DOUBLE_TYPE}, JVMWeacTypes.VOID_TYPE, false));

                            valueStack.push(new ConstantValue(JVMWeacTypes.INTERVAL_TYPE));
                            staticness.pop();
                            staticness.pop();
                            staticness.setCurrent(false).push();
                        }
                        break;

                        default:
                            System.err.println("UNRESOLVED OP: " + op.name());
                            break;
                    }
                }
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.NEW_LOCAL) {
                NewLocalVar localVar = ((NewLocalVar) precompiledInsn);
                WeacType type = resolver.resolveType(new Identifier(localVar.getType(), true), context);
                varMap.registerLocal(localVar.getName(), type);
            } else if(precompiledInsn.getOpcode() == PrecompileOpcodes.NEW) {
                InstanciateInsn instanciateInsn = (InstanciateInsn) precompiledInsn;
                WeacType type = resolver.resolveType(new Identifier(instanciateInsn.getTypeName(), false), context);
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
            } else if(precompiledInsn instanceof CastPreInsn) {
                WeacType destination = resolver.resolveType(new Identifier(((CastPreInsn) precompiledInsn).getType()), context);
                Value from = valueStack.pop();
                insns.add(new CastInsn(from.getType(), destination));
                valueStack.push(new ConstantValue(destination));
            } else if(precompiledInsn instanceof PopInstanceStack) {
                currentVarType = selfType;
            } else {
                System.err.println("UNRESOLVED: "+precompiledInsn);
            }
        }

        insns.add(0, new LocalVariableTableInsn(varMap));
        return insns;
    }

    private WeacType extractType(ResolvedInsn number) {
        switch (number.getOpcode()) {
            case ResolveOpcodes.LOAD_BYTE_CONSTANT:
                return JVMWeacTypes.BYTE_TYPE;

            case ResolveOpcodes.LOAD_DOUBLE_CONSTANT:
                return JVMWeacTypes.DOUBLE_TYPE;

            case ResolveOpcodes.LOAD_FLOAT_CONSTANT:
                return JVMWeacTypes.FLOAT_TYPE;

            case ResolveOpcodes.LOAD_INTEGER_CONSTANT:
                return JVMWeacTypes.INTEGER_TYPE;

            case ResolveOpcodes.LOAD_LONG_CONSTANT:
                return JVMWeacTypes.LONG_TYPE;

            case ResolveOpcodes.LOAD_SHORT_CONSTANT:
                return JVMWeacTypes.SHORT_TYPE;

        }
        return JVMWeacTypes.VOID_TYPE;
    }

    private int findPreviousArrayStart(int index, List<PrecompiledInsn> insns) {
        for(int i = index;i>=0;i--) {
            if(insns.get(i).getOpcode() == PrecompileOpcodes.ARRAY_START) {
                return i;
            }
        }
        return -1;
    }

    private ResolvedInsn createOperatorInsn(WeacType resultType, EnumOperators op) {
        return operatorsInsnFactories.get(op).apply(resultType);
    }
}
