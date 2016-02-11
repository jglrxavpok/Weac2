package org.jglrxavpok.weac.precompile;

import org.jglrxavpok.weac.WeacCompilePhase;
import org.jglrxavpok.weac.parse.structure.*;
import org.jglrxavpok.weac.patterns.WeacInstructionPattern;
import org.jglrxavpok.weac.precompile.patterns.WeacIntervalPattern;
import org.jglrxavpok.weac.precompile.structure.*;
import org.jglrxavpok.weac.utils.EnumOperators;
import org.jglrxavpok.weac.precompile.insn.*;

import java.util.*;

public class WeacPreCompiler extends WeacCompilePhase<WeacParsedSource, WeacPrecompiledSource> {

    public static final char[] extraDigits = (
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ"+
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase()+
            "-_"
    ).toCharArray();

    private final List<WeacInstructionPattern<WeacPrecompiledInsn>> patterns;
    private final WeacTokenizer tokenizer;

    public WeacPreCompiler() {
        patterns = new ArrayList<>();
        patterns.add(new WeacIntervalPattern());
        tokenizer = new WeacTokenizer();
    }

    @Override
    public WeacPrecompiledSource process(WeacParsedSource parsed) {
        WeacPrecompiledSource source = new WeacPrecompiledSource();

        source.classes = new ArrayList<>();

        source.imports = parsed.imports;

        source.packageName = parsed.packageName;

        parsed.classes.forEach(c -> {
            WeacPrecompiledClass clazz = precompile(c);
            clazz.imports.addAll(source.imports);
            source.classes.add(clazz);
        });

        return source;
    }

    private WeacPrecompiledClass precompile(WeacParsedClass c) {
        WeacPrecompiledClass clazz = new WeacPrecompiledClass();
        clazz.access = c.access;
        clazz.annotations.addAll(precompileAnnotations(c.annotations));
        clazz.classType = c.classType;
        clazz.enumConstants.addAll(precompileEnumConstants(c.enumConstants));
        clazz.fields.addAll(precompileFields(c.fields));
        clazz.interfacesImplemented.addAll(c.interfacesImplemented);
        clazz.isAbstract = c.isAbstract;
        clazz.isMixin = c.isMixin;
        clazz.methods.addAll(precompileMethods(c.methods));
        clazz.motherClass = c.motherClass;
        clazz.name = c.name;
        clazz.packageName = c.packageName;
        clazz.isCompilerSpecial = c.isCompilerSpecial;

        clazz.fullName = c.packageName == null || c.packageName.isEmpty() ? c.name : c.packageName+"."+c.name;
        return clazz;
    }

    private List<WeacPrecompiledAnnotation> precompileAnnotations(List<WeacParsedAnnotation> annotations) {
        List<WeacPrecompiledAnnotation> precompiledAnnotations = new LinkedList<>();
        for(WeacParsedAnnotation a : annotations) {
            WeacPrecompiledAnnotation precompiled = new WeacPrecompiledAnnotation(a.getName());
            a.getArgs().stream()
                    .map(this::precompileExpression)
                    .forEach(precompiled.getArgs()::add);

            precompiledAnnotations.add(precompiled);
        }
        return precompiledAnnotations;
    }

    private List<WeacPrecompiledMethod> precompileMethods(List<WeacParsedMethod> methods) {
        List<WeacPrecompiledMethod> precompiledMethods = new LinkedList<>();
        methods.stream()
                .map(this::compileSingleMethod)
                .forEach(precompiledMethods::add);
        return precompiledMethods;
    }

    private WeacPrecompiledMethod compileSingleMethod(WeacParsedMethod parsedMethod) {
        WeacPrecompiledMethod method = new WeacPrecompiledMethod();
        method.access = parsedMethod.access;
        method.argumentNames.addAll(parsedMethod.argumentNames);
        method.argumentTypes.addAll(parsedMethod.argumentTypes);
        method.isAbstract = parsedMethod.isAbstract;
        method.isConstructor = parsedMethod.isConstructor;
        method.name = parsedMethod.name;
        method.returnType = parsedMethod.returnType;

        method.isCompilerSpecial = parsedMethod.isCompilerSpecial;
        method.annotations.addAll(precompileAnnotations(parsedMethod.annotations));

        method.instructions.addAll(flatten(compileCodeBlock(parsedMethod.methodSource)));

        return method;
    }

    private List<WeacPrecompiledInsn> flatten(WeacCodeBlock weacCodeBlock) {
        // TODO
        List<WeacPrecompiledInsn> out = new LinkedList<>();
        weacCodeBlock.getInstructions().forEach(out::addAll);
        return out;
    }

    private WeacCodeBlock compileCodeBlock(String source) {

        // TODO: Support if, else, etc.
        char[] chars = source.toCharArray();
        StringBuilder buffer = new StringBuilder();
        final WeacCodeBlock currentBlock = new WeacCodeBlock();

        WeacCodeBlock previousBlock = new WeacCodeBlock();

        List<List<WeacPrecompiledInsn>> instructions = currentBlock.getInstructions();
        instructions.add(Collections.singletonList(new WeacLabelInsn(currentBlock.getStart())));

        int offset = 0;
        String instruction;
        do {
            instructions.add(Collections.singletonList(new WeacLabelInsn()));
            offset += readUntilNot(chars, offset, ' ', '\n').length();
            instruction = readUntilInsnEnd(chars, offset);
            offset += instruction.length() + 1;
            instructions.add(precompileExpression(instruction));
        } while(!instruction.isEmpty());

/*        for(int i = 0;i<chars.length;i++) {
            char c = chars[i];
            if(c == '(') {
                int offset = handleBuiltins(buffer.toString(), chars, i, instructions, currentBlock, previousBlock);
                if(offset == 0) {
                    buffer.append(c);
                } else {
                    buffer.delete(0, buffer.length());
                }
                i += offset;
            } else if(c == '{') {
                if(buffer.toString().equals("else")) {
                    // TODO
                } else {
                    String codeBlockSource = readCodeblock(chars, i+1);
                    WeacCodeBlock block = compileCodeBlock(codeBlockSource);
                    instructions.add(Collections.singletonList(new WeacBlockInsn(block)));
                    i += codeBlockSource.length();
                }
                buffer.delete(0, buffer.length());
            } else {
                String instructionSource = readUntilInsnEnd(chars, i);
                buffer.delete(0, buffer.length());
                instructions.add(compileSingleInstruction(instructionSource));
            }
        }*/
        instructions.add(Collections.singletonList(new WeacLabelInsn(currentBlock.getEnd())));
        return currentBlock;
    }

    private List<WeacPrecompiledField> precompileFields(List<WeacParsedField> fields) {
        List<WeacPrecompiledField> finalFields = new LinkedList<>();
        for(WeacParsedField f : fields) {
            WeacPrecompiledField precompiledField = new WeacPrecompiledField();
            precompiledField.access = f.access;
            precompiledField.name = f.name;
            precompiledField.type = f.type;
            precompiledField.isCompilerSpecial = f.isCompilerSpecial;
            precompiledField.defaultValue.addAll(precompileExpression(f.defaultValue));
            finalFields.add(precompiledField);

            precompiledField.annotations.addAll(precompileAnnotations(f.annotations));
        }
        return finalFields;
    }

    private List<WeacPrecompiledEnumConstant> precompileEnumConstants(List<String> enumConstants) {
        List<WeacPrecompiledEnumConstant> constants = new ArrayList<>();
        for(String constant : enumConstants) {
            if(constant.contains("(")) {
                String name = constant.substring(0, constant.indexOf('('));
                WeacPrecompiledEnumConstant precompiledConstant = new WeacPrecompiledEnumConstant();
                precompiledConstant.name = name;

                char[] chars = constant.toCharArray();
                int offset = name.length()+1; // +1 to avoid the '('
                String arg = readSingleArgument(name, offset, false);
                while(!arg.isEmpty()) {
                    arg = readSingleArgument(name, offset, false);
                    offset += arg.length()+1;
                    offset += readUntilNot(chars, offset, ' ', '\n').length();

                    precompiledConstant.parameters.add(precompileExpression(arg));
                }

            } else {
                WeacPrecompiledEnumConstant precompiledConstant = new WeacPrecompiledEnumConstant();
                precompiledConstant.name = constant;
                constants.add(precompiledConstant);
            }
        }
        return constants;
    }

    public List<WeacPrecompiledInsn> precompileExpression(String expression) {
        if(expression == null) {
            return Collections.emptyList();
        }
        List<WeacPrecompiledInsn> insns = new LinkedList<>();
        char[] chars = expression.toCharArray();
        List<WeacToken> tokens = new LinkedList<>();
        for(int i = 0;i<chars.length;) {
            WeacToken token = tokenizer.nextToken(chars, i);
            if(token != null) {
                i += token.length;
                tokens.add(token);
            } else {
                break; // reached end of file
            }
        }
        Iterator<WeacToken> iterator = tokens.iterator();
        while(iterator.hasNext()) {
            WeacToken token = iterator.next();
            if (token.getType() == WeacTokenType.WAITING_FOR_NEXT)
                iterator.remove();
        }

        iterator = tokens.iterator();
        WeacToken previous = null;
        while(iterator.hasNext()) {
            WeacToken token = iterator.next();
            if(previous != null) {
                if(previous.getType() == WeacTokenType.LITERAL) {
                    if(token.getType() == WeacTokenType.OPENING_PARENTHESIS) {
                        previous.setType(WeacTokenType.FUNCTION);
                    } else {
                        previous.setType(WeacTokenType.VARIABLE);
                    }
                } else if(previous.getType() == WeacTokenType.OPERATOR) {
                    if(token.getType() == WeacTokenType.CLOSING_PARENTHESIS) {
                        previous.setType(WeacTokenType.UNARY_OPERATOR);
                    } else {
                        previous.setType(WeacTokenType.BINARY_OPERATOR);
                    }
                } else if(token.getType() == WeacTokenType.OPERATOR) {
                    if(previous.getType() == WeacTokenType.OPENING_PARENTHESIS) {
                        token.setType(WeacTokenType.UNARY_OPERATOR);
                    }
                }
            }

            if(token.getType() == WeacTokenType.LITERAL && !iterator.hasNext()) {
                token.setType(WeacTokenType.VARIABLE);
            }

            if(token.getType() == WeacTokenType.OPERATOR && previous == null) {
                token.setType(WeacTokenType.UNARY_OPERATOR);
            }

            previous = token;
        }
        handleBuiltins(tokens);
        tokens.forEach(t -> System.out.println("token: "+t));

        // TODO: convert 'ELSE' + 'IF' to 'ELSE IF'
        List<WeacToken> output = convertToRPN(expression, tokens);

        for(WeacToken token : output) {
            System.out.print(token.getType().name()+"("+token.getContent()+") ");
        }
        System.out.println();
        return toInstructions(output, insns);
    }

    private void handleBuiltins(List<WeacToken> tokens) {
        ListIterator<WeacToken> it = tokens.listIterator();
        while (it.hasNext()) {
            WeacToken t = it.next();
            if(t.getType() == WeacTokenType.FUNCTION) {
                String name = t.getContent();
                if(name.equals("if")) {
                    t.setType(WeacTokenType.IF);
                }
            } else if(t.getType() == WeacTokenType.VARIABLE || t.getType() == WeacTokenType.LITERAL) {
                switch (t.getContent()) {
                    case "true":
                    case "false":
                        t.setType(WeacTokenType.BOOLEAN);
                        break;

                    case "this":
                        t.setType(WeacTokenType.THIS);
                        break;

                    case "else":
                        t.setType(WeacTokenType.ELSE);
                        break;
                }
            }
        }
    }

    private List<WeacPrecompiledInsn> toInstructions(List<WeacToken> output, List<WeacPrecompiledInsn> insns) {
        // TODO: Handle 'new' after function calls
        for(WeacToken token : output) {
            switch (token.getType()) {
                case ARGUMENT_SEPARATOR:
                    insns.add(new WeacSimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR));
                    break;

                case NUMBER:
                    insns.add(new WeacLoadNumberConstant(token.getContent()));
                    break;

                case BOOLEAN:
                    insns.add(new WeacLoadBooleanConstant(Boolean.parseBoolean(token.getContent())));
                    break;

                case STRING:
                    insns.add(new WeacLoadStringConstant(token.getContent()));
                    break;

                case SINGLE_CHARACTER:
                    insns.add(new WeacLoadCharacterConstant(token.getContent()));
                    break;

                case VARIABLE:
                    insns.add(new WeacLoadVariable(token.getContent()));
                    break;

                case FUNCTION:
                    String[] contents = token.getContent().split(";");
                    String name = contents[0];
                    int argCount = Integer.parseInt(contents[1]);
                    boolean lookForInstance = Boolean.parseBoolean(contents[2]);
                    insns.add(new WeacFunctionCall(name, argCount, lookForInstance));
                    break;

                case BINARY_OPERATOR:
                    insns.add(new WeacOperatorInsn(EnumOperators.get(token.getContent(), false)));
                    break;

                case THIS:
                    insns.add(new WeacPrecompiledLoadThis());
                    break;

                case UNARY_OPERATOR:
                    EnumOperators operator = EnumOperators.get(token.getContent(), true);
                    if(operator == EnumOperators.RETURN) {
                        insns.add(new WeacSimplePreInsn(PrecompileOpcodes.RETURN));
                    } else if(operator == EnumOperators.NEW) {
                        WeacPrecompiledInsn prev = insns.remove(insns.size()-1);
                        String typeName;
                        int constructorArgCount = 0;
                        if(prev.getOpcode() == PrecompileOpcodes.LOAD_VARIABLE) {
                            WeacLoadVariable var = (WeacLoadVariable)prev;
                            typeName = var.getName();
                        } else if(prev.getOpcode() == PrecompileOpcodes.FUNCTION_CALL) {
                            WeacFunctionCall call = (WeacFunctionCall)prev;
                            typeName = call.getName();
                            constructorArgCount = call.getArgCount();
                            if(call.shouldLookForInstance()) {
                                newError("Incorrect call to constructor", -1); // todo: line
                            }
                        } else {
                            newError("Invalid token before constructor (opcode is "+prev.getOpcode()+")", -1); // todo: line
                            typeName = "INVALID$$";
                        }
                        insns.add(new WeacInstanciateInsn(typeName));

                        // look into the stack, as the value as just be added via the WeacInstanceInsn
                        insns.add(new WeacFunctionCall("<init>", constructorArgCount, true));

                    } else {
                        insns.add(new WeacOperatorInsn(operator));
                    }
                    break;

                case DEFINE_ARRAY:
                    int length = Integer.parseInt(token.getContent());
                    insns.add(new WeacCreateArray(length, "$$unknown"));
                    for(int i = 0;i<length;i++)
                        insns.add(new WeacStoreArray(length-i-1));
                    break;

                default:
                    System.err.println("Precompilation: unknown "+token);
                    break;

            }
        }

        return postProcessInstructions(insns);
    }

    private List<WeacPrecompiledInsn> postProcessInstructions(List<WeacPrecompiledInsn> insns) {
        List<WeacPrecompiledInsn> finalInstructions = new LinkedList<>();
        finalInstructions.add(new WeacLabelInsn());
        for(int i = 0;i<insns.size();i++) {
            boolean matchFound = false;
            for(WeacInstructionPattern<WeacPrecompiledInsn> p : patterns) {
                if(p.matches(insns, i)) {
                    p.output(insns, i, finalInstructions);
                    i += p.consumeCount(insns, i);
                    matchFound = true;
                }
            }

            if(!matchFound) {
                finalInstructions.add(insns.get(i));
            }

        }
        finalInstructions.add(new WeacLabelInsn());
        return finalInstructions;
    }

    /**
     * Implementation of <a href="https://en.wikipedia.org/wiki/Shunting-yard_algorithm">Edsger Dijkstra's Shuting-Yard Algorithm</a>
     * @param expr
     * @param tokens
     * @return
     */
    private List<WeacToken> convertToRPN(String expr, List<WeacToken> tokens) {
        List<WeacToken> out = new ArrayList<>();
        Stack<WeacToken> stack = new Stack<>();
        int argCount = 0;
        Stack<Integer> argCountStack = new Stack<>();
        Stack<Boolean> instanceStack = new Stack<>();
        instanceStack.push(false);
        for(int i = 0;i<tokens.size();i++) {
            WeacToken token = tokens.get(i);
            if(token.getType() == WeacTokenType.NUMBER || token.getType() == WeacTokenType.STRING
                    || token.getType() == WeacTokenType.SINGLE_CHARACTER || token.getType() == WeacTokenType.VARIABLE
                    || token.getType() == WeacTokenType.BOOLEAN
                    || token.getType() == WeacTokenType.THIS) {
                if(i+2 < tokens.size()) {
                    if(tokens.get(i+1).getType() == WeacTokenType.MEMBER_ACCESSING) {
                        WeacTokenType type = tokens.get(i + 2).getType();
                        instanceStack.push(true);
                        if(type == WeacTokenType.VARIABLE || type == WeacTokenType.THIS) {
                            out.add(token);
                            out.add(tokens.get(i+1));
                            out.add(tokens.get(i+2));
                            argCount++;
                            i+=2;
                        } else { // it is a method
                            stack.push(tokens.get(i+1));
                            out.add(token);
                            i++; // skip the '.'
                        }
                    } else {
                        out.add(token);
                        argCount++;
                    }
                } else {
                    out.add(token);
                    argCount++;
                }
            } else if(token.getType() == WeacTokenType.FUNCTION || token.getType() == WeacTokenType.IF) {
                stack.push(token);
                argCountStack.push(argCount);
                argCount = 0;
            } else if(token.getType() == WeacTokenType.ARGUMENT_SEPARATOR) {
                out.add(token);
                if(stack.isEmpty()) {
                    newError("Unmatched parenthesises, please fix", -1);
                    return Collections.EMPTY_LIST;
                }
                while(!stack.peek().isOpeningBracketLike()) {
                    out.add(stack.pop());
                    if(stack.isEmpty()) {
                        newError("Unmatched parenthesises, please fix", -1);
                        return Collections.EMPTY_LIST;
                    }
                }
            } else if(token.getType() == WeacTokenType.UNARY_OPERATOR || token.getType() == WeacTokenType.BINARY_OPERATOR) {
                EnumOperators operator = EnumOperators.get(token.getContent(), token.getType() == WeacTokenType.UNARY_OPERATOR);
                if(operator != null) {
                    while (!stack.isEmpty() && (stack.peek().getType() == WeacTokenType.UNARY_OPERATOR || stack.peek().getType() == WeacTokenType.BINARY_OPERATOR)) {
                        WeacToken stackTop = stack.pop();
                        EnumOperators operator2 = EnumOperators.get(stackTop.getContent(), stackTop.getType() == WeacTokenType.UNARY_OPERATOR);
                        if (operator2 != null) {
                            if (operator.associativity() == EnumOperators.Associativity.LEFT) {
                                if (operator.precedence() > operator2.precedence()) {
                                    out.add(stackTop);
                                    if(!operator2.unary()) {
                                        argCount--;
                                    }
                                } else {
                                    break;
                                }
                            } else {
                                if (operator.precedence() >= operator2.precedence()) {
                                    out.add(stackTop);
                                    if(!operator2.unary()) {
                                        argCount--;
                                    }
                                } else {
                                    break;
                                }
                            }
                        } else {
                            newError("dzqdzqd", -1);
                            break;
                        }
                    }
                    stack.push(token);
                } else {
                    newError("dzqdzqd", -1);
                }
            } else if(token.isOpeningBracketLike()) {
                stack.push(token);
                if(token.getType() == WeacTokenType.OPENING_CURLY_BRACKETS) {
                    out.add(token);
                } else if(token.getType() == WeacTokenType.OPENING_SQUARE_BRACKETS) {
                    argCountStack.push(argCount);
                    argCount = 0;
                }
            } else if(token.isClosingBracketLike()) {
                if(!stack.isEmpty()) {
                    while(!stack.peek().isOpposite(token)) {
                        out.add(stack.pop());
                    }

                    if(token.getType() == WeacTokenType.CLOSING_SQUARE_BRACKETS) {
                        stack.pop(); // pop opening bracket
                        out.add(new WeacToken(""+argCount, WeacTokenType.DEFINE_ARRAY, -1));
                        argCount = argCountStack.pop();
                        argCount++;
                    } else {
                        if(stack.isEmpty()) {
                            newError("Unmatched parenthesises, please fix", -1);
                            return Collections.EMPTY_LIST;
                        } else {
                            WeacToken previous = stack.pop();
                            if(previous.getType() == WeacTokenType.OPENING_SQUARE_BRACKETS) {
                                argCount++;
                            } else {
                                if(!stack.isEmpty()) {
                                    WeacToken top = stack.peek();
                                    if(top.getType() == WeacTokenType.FUNCTION || top.getType() == WeacTokenType.IF) {
                                        WeacToken originalToken = stack.pop();
                                        boolean shouldLookForInstance = false;
                                        /*if(!stack.isEmpty()) {
                                            if(stack.peek().getType() == WeacTokenType.MEMBER_ACCESSING) {
                                                shouldLookForInstance = true;
                                                stack.pop();
                                            }
                                        } else if(stack.isEmpty() && out.size()-argCount > 0) {
                                            shouldLookForInstance = true;
                                        }*/
                                        shouldLookForInstance = instanceStack.pop();
                                        // function name;argument count;true if we should look for the object to call it on in the stack
                                        WeacToken functionToken = new WeacToken(originalToken.getContent()+";"+argCount+";"+String.valueOf(shouldLookForInstance), top.getType(), originalToken.length);
                                        argCount = argCountStack.pop();
                                        argCount++;
                                        out.add(functionToken);
                                    } else {
                                        System.out.println("FREAKING OUT "+Arrays.toString(stack.toArray())+" / "+top+" / "+previous);
                                    }
                                }
                            }

                            if(token.getType() == WeacTokenType.CLOSING_CURLY_BRACKETS) {
                                out.add(token);
                            }
                        }
                    }
                } else {
                    newError("Unmatched parenthesises, please fix", -1);
                    return Collections.EMPTY_LIST;
                }
            } else if(token.getType() == WeacTokenType.IF || token.getType() == WeacTokenType.IF) {
                out.add(token);
            }
        }
        while(!stack.isEmpty()) {
            WeacToken token = stack.pop();
            if(token.isOpeningBracketLike()) {
                newError("Unmatched parenthesis in "+expr+", found "+token+" instead of opening parenthesis", -1);
                break;
            }
            out.add(token);
        }
        return out;
    }

    @Override
    public Class<WeacParsedSource> getInputClass() {
        return WeacParsedSource.class;
    }

    @Override
    public Class<WeacPrecompiledSource> getOutputClass() {
        return WeacPrecompiledSource.class;
    }
}
