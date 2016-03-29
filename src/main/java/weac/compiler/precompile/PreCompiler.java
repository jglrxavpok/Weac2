package weac.compiler.precompile;

import weac.compiler.CompilePhase;
import weac.compiler.parse.structure.*;
import weac.compiler.patterns.InstructionPattern;
import weac.compiler.precompile.insn.*;
import weac.compiler.precompile.patterns.*;
import weac.compiler.precompile.structure.*;
import weac.compiler.utils.EnumOperators;
import org.jglr.flows.collection.VariableTopStack;
import weac.compiler.utils.GenericType;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.WeacType;

import java.util.*;

public class PreCompiler extends CompilePhase<ParsedSource, PrecompiledSource> {

    public static final char[] extraDigits = (
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ"+
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase()+
            "-_"
    ).toCharArray();

    private final List<InstructionPattern<PrecompiledInsn>> patterns;
    private final List<TokenPattern> tokenPatterns;
    private final Tokenizer tokenizer;

    public PreCompiler() {
        patterns = new ArrayList<>();
        patterns.add(new IntervalPattern());

        tokenPatterns = new ArrayList<>();
        tokenPatterns.add(new CastPattern());
        tokenPatterns.add(new LocalCreationPattern());
        tokenPatterns.add(new ElseIfPattern());

        tokenizer = new Tokenizer();
    }

    @Override
    public PrecompiledSource process(ParsedSource parsed) {
        PrecompiledSource source = new PrecompiledSource();

        source.classes = new ArrayList<>();

        source.imports = parsed.imports;

        source.packageName = parsed.packageName;
        source.fileName = parsed.fileName;

        parsed.classes.forEach(c -> {
            PrecompiledClass clazz = precompile(c);
            clazz.imports.addAll(source.imports);
            source.classes.add(clazz);
        });

        return source;
    }

    private PrecompiledClass precompile(ParsedClass c) {
        PrecompiledClass clazz = new PrecompiledClass();
        if(c.name.isGeneric()) {
            WeacType[] params = c.name.getGenericParameters();
            for (WeacType param : params) {
                clazz.getGenericParameterNames().add(new GenericType(param));
            }
        }

        clazz.access = c.access;
        clazz.annotations.addAll(precompileAnnotations(c.annotations));
        clazz.classType = c.classType;
        clazz.enumConstants.addAll(precompileEnumConstants(c.enumConstants));
        clazz.fields.addAll(precompileFields(c.fields, clazz));
        clazz.interfacesImplemented.addAll(c.interfacesImplemented);
        clazz.isAbstract = c.isAbstract;
        clazz.isMixin = c.isMixin;
        clazz.methods.addAll(precompileMethods(c.methods, clazz));
        clazz.motherClass = c.motherClass;
        clazz.name = c.name.getCoreType();


        clazz.packageName = c.packageName;
        clazz.isCompilerSpecial = c.isCompilerSpecial;
        clazz.isFinal = c.isFinal;

        clazz.fullName = c.packageName == null || c.packageName.isEmpty() ? c.name.getCoreType().getIdentifier().getId() : c.packageName+"."+c.name.getCoreType().getIdentifier().getId();
        return clazz;
    }

    private List<PrecompiledAnnotation> precompileAnnotations(List<ParsedAnnotation> annotations) {
        List<PrecompiledAnnotation> precompiledAnnotations = new LinkedList<>();
        for(ParsedAnnotation a : annotations) {
            PrecompiledAnnotation precompiled = new PrecompiledAnnotation(a.getName());
            a.getArgs().stream()
                    .map(this::precompileExpression)
                    .forEach(precompiled.getArgs()::add);

            precompiledAnnotations.add(precompiled);
        }
        return precompiledAnnotations;
    }

    private List<PrecompiledMethod> precompileMethods(List<ParsedMethod> methods, PrecompiledClass clazz) {
        List<PrecompiledMethod> precompiledMethods = new LinkedList<>();
        methods.stream()
                .map(m -> compileSingleMethod(m, clazz))
                .forEach(precompiledMethods::add);
        return precompiledMethods;
    }

    private PrecompiledMethod compileSingleMethod(ParsedMethod parsedMethod, PrecompiledClass clazz) {
        PrecompiledMethod method = new PrecompiledMethod();
        method.access = parsedMethod.access;
        method.argumentNames.addAll(parsedMethod.argumentNames);
        for(Identifier t : parsedMethod.argumentTypes) {
            method.argumentTypes.add(resolveGeneric(t, clazz));
        }
        method.isAbstract = parsedMethod.isAbstract;
        method.isConstructor = parsedMethod.isConstructor;
        method.name = parsedMethod.name;
        method.returnType = resolveGeneric(parsedMethod.returnType, clazz);

        method.isCompilerSpecial = parsedMethod.isCompilerSpecial;
        method.annotations.addAll(precompileAnnotations(parsedMethod.annotations));

        method.instructions.addAll(flatten(compileCodeBlock(parsedMethod.methodSource)));

        return method;
    }

    private List<PrecompiledInsn> flatten(CodeBlock codeBlock) {
        // TODO
        List<PrecompiledInsn> out = new LinkedList<>();
        codeBlock.getInstructions().forEach(out::addAll);
        return out;
    }

    private CodeBlock compileCodeBlock(String source) {

        char[] chars = source.toCharArray();
        StringBuilder buffer = new StringBuilder();
        final CodeBlock currentBlock = new CodeBlock();

        CodeBlock previousBlock = new CodeBlock();

        List<List<PrecompiledInsn>> instructions = currentBlock.getInstructions();
        instructions.add(Collections.singletonList(new LabelInsn(currentBlock.getStart())));

        int offset = 0;
        String instruction;
        int labelIndex = -1;
        // TODO: Increment label
        do {
            instructions.add(Collections.singletonList(new LabelInsn(new Label(labelIndex))));
            offset += readUntilNot(chars, offset, ' ', '\n').length();
            instruction = readUntilInsnEnd(chars, offset);
            offset += instruction.length() + 1;
            instructions.add(precompileExpression(instruction));
        } while(!instruction.isEmpty());

        instructions.add(Collections.singletonList(new LabelInsn(currentBlock.getEnd())));
        return currentBlock;
    }

    private List<PrecompiledField> precompileFields(List<ParsedField> fields, PrecompiledClass clazz) {
        List<PrecompiledField> finalFields = new LinkedList<>();
        for(ParsedField f : fields) {
            PrecompiledField precompiledField = new PrecompiledField();
            precompiledField.access = f.access;
            precompiledField.name = f.name;
            precompiledField.type = resolveGeneric(f.type, clazz);
            precompiledField.isCompilerSpecial = f.isCompilerSpecial;
            precompiledField.defaultValue.addAll(precompileExpression(f.defaultValue));
            finalFields.add(precompiledField);

            precompiledField.annotations.addAll(precompileAnnotations(f.annotations));
        }
        return finalFields;
    }

    private Identifier resolveGeneric(Identifier type, PrecompiledClass clazz) {
        boolean isGeneric = clazz.getGenericParameterNames().stream()
                .filter(t -> t.getIdentifier().getId().endsWith(type.getId()))
                .count() != 0L;
        return isGeneric ? WeacType.OBJECT_TYPE.getIdentifier() : type;
    }

    private List<PrecompiledEnumConstant> precompileEnumConstants(List<String> enumConstants) {
        List<PrecompiledEnumConstant> constants = new ArrayList<>();
        for(String constant : enumConstants) {
            if(constant.contains("(")) {
                String name = constant.substring(0, constant.indexOf('('));
                PrecompiledEnumConstant precompiledConstant = new PrecompiledEnumConstant();
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
                PrecompiledEnumConstant precompiledConstant = new PrecompiledEnumConstant();
                precompiledConstant.name = constant;
                constants.add(precompiledConstant);
            }
        }
        return constants;
    }

    public List<PrecompiledInsn> precompileExpression(String expression) {
        return precompileExpression(expression, false);
    }

    public List<PrecompiledInsn> precompileExpression(String expression, boolean ditchLabels) {
        if(expression == null) {
            return Collections.emptyList();
        }
        List<PrecompiledInsn> insns = new LinkedList<>();
        char[] chars = expression.toCharArray();
        List<Token> tokens = new LinkedList<>();
        for(int i = 0;i<chars.length;) {
            Token token = tokenizer.nextToken(chars, i);
            if(token != null) {
                i += token.length;
                tokens.add(token);
            } else {
                break; // reached end of file
            }
        }
        ListIterator<Token> iterator = tokens.listIterator();
        while(iterator.hasNext()) {
            Token token = iterator.next();
            if (token.getType() == TokenType.WAITING_FOR_NEXT)
                iterator.remove();
        }

        iterator = tokens.listIterator();
        Token previous = null;
        while(iterator.hasNext()) {
            Token token = iterator.next();
            if(previous != null) {
                if(previous.getType() == TokenType.LITERAL) {
                    if(token.getType() == TokenType.OPENING_PARENTHESIS) {
                        previous.setType(TokenType.FUNCTION);
                    } else {
                        previous.setType(TokenType.VARIABLE);
                    }
                }
            }

            if(token.getType() == TokenType.LITERAL && !iterator.hasNext()) {
                token.setType(TokenType.VARIABLE);
            }

            previous = token;
        }

        resolveOperators(tokens);

        handleBuiltins(tokens);

        tokens = solvePatterns(tokens);

        System.out.println("START TOKENS OF "+expression);
        tokens.forEach(t -> System.out.print("<"+t+"> "));
        System.out.println("\nEND TOKENS");

        List<Token> output = convertToRPN(expression, tokens);


        List<PrecompiledInsn> instructions = toInstructions(output, insns);
        if(ditchLabels) {
            ListIterator<PrecompiledInsn> insnIterator = instructions.listIterator();
            while (insnIterator.hasNext()) {
                PrecompiledInsn insn = insnIterator.next();
                if(insn instanceof LabelInsn) {
                    if(((LabelInsn) insn).getLabel().getIndex() < 0)
                        insnIterator.remove();
                    // TODO: Remove only if not necessary
                }
            }
        }
        return instructions;
    }

    private void resolveOperators(List<Token> tokens) {
        ListIterator<Token> iterator = tokens.listIterator();
        while (iterator.hasNext()) {
            Token previous = null;
            Token next = null;
            if(iterator.hasPrevious()) {
                previous = tokens.get(iterator.previousIndex());
            }
            Token token = iterator.next();
            if(iterator.hasNext()) {
                next = tokens.get(iterator.nextIndex());
            }
            if (token.getType() == TokenType.OPERATOR) {
                if(EnumOperators.isAmbiguous(token.getContent())) {
                    if(next == null || previous == null) {
                        token.setType(TokenType.UNARY_OPERATOR);
                    } else {
                        if(!previous.isOpeningBracketLike() && !next.isClosingBracketLike()) {
                            token.setType(TokenType.BINARY_OPERATOR);
                        } else {
                            token.setType(TokenType.UNARY_OPERATOR);
                        }
                    }
                } else {
                    token.setType(EnumOperators.get(token.getContent()).isUnary() ? TokenType.UNARY_OPERATOR : TokenType.BINARY_OPERATOR);
                }
            }
        }
    }

    private List<Token> solvePatterns(List<Token> tokens) {
        List<Token> finalTokens = new LinkedList<>();
        for(int i = 0;i<tokens.size();i++) {
            boolean matchFound = false;
            for(TokenPattern p : tokenPatterns) {
                if(p.matches(tokens, i)) {
                    p.output(tokens, i, finalTokens);
                    i += p.consumeCount(tokens, i)-1;
                    matchFound = true;
                }
            }

            if(!matchFound) {
                finalTokens.add(tokens.get(i));
            }

        }
        return finalTokens;
    }

    private void handleBuiltins(List<Token> tokens) {
        for (Token t : tokens) {
            if (t.getType() == TokenType.FUNCTION) {
                String name = t.getContent();
                if (name.equals("if")) {
                    t.setType(TokenType.IF);
                }
            } else if (t.getType() == TokenType.VARIABLE || t.getType() == TokenType.LITERAL) {
                switch (t.getContent()) {
                    case "true":
                    case "false":
                        t.setType(TokenType.BOOLEAN);
                        break;

                    case "this":
                        t.setType(TokenType.THIS);
                        break;

                    case "null":
                        t.setType(TokenType.NULL);
                        break;

                    case "else":
                        t.setType(TokenType.ELSE);
                        break;
                }
            }
        }
    }

    private List<PrecompiledInsn> toInstructions(List<Token> output, List<PrecompiledInsn> insns) {
        int labelIndex = 1;
        for(int i = 0;i<output.size();i++) {
            Token token = output.get(i);
            Token previous = null;
            Token next = null;
            if(i != 0)
                previous = output.get(i-1);
            if(i != output.size()-1)
                next = output.get(i+1);
            switch (token.getType()) {

                case IF:
                    if(next == null) {
                        newError("Invalid statement", -1); // todo: line
                    } else {
                        if(next.getType() == TokenType.OPENING_CURLY_BRACKETS) {
                            int index = findEndOfBlock(next, i+1, output);
                            if(index < 0) {
                                newError("Unmatched curly bracket", -1);
                            } else {
                                int firstJumpTo = labelIndex++;
                                int secondJumpTo = labelIndex++;
                                output.set(index, new Token(firstJumpTo+";"+secondJumpTo, TokenType.CLOSING_CURLY_BRACKETS, -1));
                                insns.add(new IfNotJumpInsn(new Label(firstJumpTo)));
                            }
                        } else {
                            newError("Not supported yet", -1);
                        }
                    }
                    break;

                case ELSEIF:
                    if(next == null || previous == null) {
                        newError("Invalid statement", -1); // todo: line
                    } else {
                        if(next.getType() == TokenType.OPENING_CURLY_BRACKETS) {
                            int index = findEndOfBlock(next, i+1, output);
                            int firstJumpTo = labelIndex++;
                            int secondJumpTo = labelIndex++;
                            output.set(index, new Token(firstJumpTo+";"+secondJumpTo, TokenType.CLOSING_CURLY_BRACKETS, -1));
                            insns.add(new IfNotJumpInsn(new Label(firstJumpTo)));
                        } else {
                            newError("Not supported yet "+next.getType(), -1);
                        }
                    }
                    break;

                case ELSE:
                    if(next == null || previous == null) {
                        newError("Invalid statement", -1); // todo: line
                    } else {
                        if(next.getType() == TokenType.OPENING_CURLY_BRACKETS && previous.getType() == TokenType.CLOSING_CURLY_BRACKETS) {
                            int index = findEndOfBlock(next, i+1, output);
                            int jumpTo = labelIndex++;
                            String[] jumps = previous.getContent().split(";");
                            int firstJump = Integer.parseInt(jumps[0]);
                            int secondJump = Integer.parseInt(jumps[1]);

                            output.set(index, new Token(String.valueOf(jumpTo), TokenType.CLOSING_CURLY_BRACKETS, -1));
                            insns.add(new GotoInsn(new Label(firstJump)));

                            insns.add(new LabelInsn(new Label(secondJump)));
                            insns.add(new GotoInsn(new Label(jumpTo)));

                            insns.add(new LabelInsn(new Label(firstJump)));
                        } else {
                            newError("Not supported yet", -1);
                        }
                    }
                    break;

                case OPENING_CURLY_BRACKETS:
                    int index = findEndOfBlock(token, i, output);
                    if(index == -1) { // should not happen, but who knows
                        newError("Unclosed curly bracket!", -1); // todo: line
                    }
                    break;

                case CLOSING_CURLY_BRACKETS:
                    if(next == null || next.getType() != TokenType.ELSE) {
                        if(next != null && next instanceof FunctionStartToken) {
                            if(((FunctionStartToken) next).getFunctionType() == TokenType.ELSEIF) {
                                break;
                            }
                        }
                        if(!token.getContent().equals("}")) {
                            int lbl = Integer.parseInt(token.getContent().split(";")[0]);
                            insns.add(new LabelInsn(new Label(lbl+1)));
                            insns.add(new LabelInsn(new Label(lbl)));
                        }
                    }
                    break;

                case NEW_LOCAL:
                    NewLocalToken localToken = ((NewLocalToken) token);
                    insns.add(new NewLocalVar(localToken.getLocalType(), localToken.getName()));
                    break;

                case CAST:
                    insns.add(new CastPreInsn(token.getContent()));
                    break;

                case ARGUMENT_SEPARATOR:
                    insns.add(new SimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR));
                    break;

                case NUMBER:
                    insns.add(new LoadNumberConstant(token.getContent()));
                    break;

                case BOOLEAN:
                    insns.add(new LoadBooleanConstant(Boolean.parseBoolean(token.getContent())));
                    break;

                case STRING:
                    insns.add(new LoadStringConstant(token.getContent()));
                    break;

                case SINGLE_CHARACTER:
                    insns.add(new LoadCharacterConstant(token.getContent()));
                    break;

                case VARIABLE:
                    insns.add(new LoadVariable(token.getContent()));
                    break;

                case FUNCTION:
                    String[] contents = token.getContent().split(";");
                    String name = contents[0];
                    int argCount = Integer.parseInt(contents[1]);
                    boolean lookForInstance = Boolean.parseBoolean(contents[2]);
                    insns.add(new FunctionCall(name, argCount, lookForInstance));
                    break;

                case BINARY_OPERATOR:
                    insns.add(new OperatorInsn(EnumOperators.get(token.getContent(), false)));
                    break;

                case THIS:
                    insns.add(new PrecompiledLoadThis());
                    break;

                case NULL:
                    insns.add(new PrecompiledLoadNull());
                    break;

                case FUNCTION_START:
                    if(token instanceof FunctionStartToken) {
                        if(((FunctionStartToken) token).getFunctionType() == TokenType.ELSEIF) {
                            if (previous != null && previous.getType() == TokenType.CLOSING_CURLY_BRACKETS && !previous.getContent().equals("}")) {
                                int jumpTo = labelIndex;
                                //insns.add(new GotoInsn(new Label(jumpTo)));

                                String[] jumps = previous.getContent().split(";");
                                int firstJump = Integer.parseInt(jumps[0]);
                                int secondJump = Integer.parseInt(jumps[1]);

                                insns.add(new LabelInsn(new Label(secondJump)));
                                insns.add(new GotoInsn(new Label(jumpTo+1)));

                                insns.add(new LabelInsn(new Label(firstJump)));
                            } else {
                                newError("Not supported, yet", -1);
                            }
                        }
                    }
                    insns.add(new SimplePreInsn(PrecompileOpcodes.FUNCTION_START));
                    break;

                case UNARY_OPERATOR:
                    EnumOperators operator = EnumOperators.get(token.getContent(), true);
                    if(operator == EnumOperators.RETURN) {
                        insns.add(new SimplePreInsn(PrecompileOpcodes.RETURN));
                    } else if(operator == EnumOperators.THROW) {
                        insns.add(new SimplePreInsn(PrecompileOpcodes.THROW));
                    } else if(operator == EnumOperators.NEW) {
                        PrecompiledInsn prev = insns.remove(insns.size()-1);
                        String typeName;
                        int constructorArgCount = 0;
                        if(prev.getOpcode() == PrecompileOpcodes.LOAD_VARIABLE) {
                            LoadVariable var = (LoadVariable)prev;
                            typeName = var.getName();
                        } else if(prev.getOpcode() == PrecompileOpcodes.FUNCTION_CALL) {
                            FunctionCall call = (FunctionCall)prev;
                            typeName = call.getName();
                            constructorArgCount = call.getArgCount();
                            //if(call.shouldLookForInstance()) {
                            //    newError("Incorrect call to constructor "+prev, -1); // todo: line
                            //}
                        } else {
                            newError("Invalid token before constructor (opcode is "+prev.getOpcode()+")", -1); // todo: line
                            typeName = "INVALID$$";
                        }
                        insns.add(new InstanciateInsn(typeName));
                        insns.add(new SimplePreInsn(PrecompileOpcodes.DUP));

                        // look into the stack, as the value as just be added via the WeacInstanceInsn
                        insns.add(new FunctionCall("<init>", constructorArgCount, true));

                    } else {
                        insns.add(new OperatorInsn(operator));
                    }
                    break;

                case DEFINE_ARRAY:
                    int length = Integer.parseInt(token.getContent());
                    insns.add(new CreateArray(length, "$$unknown"));
                    for(int j = 0;j<length;j++) {
                        insns.add(new SimplePreInsn(PrecompileOpcodes.DUP));
                        insns.add(new StoreArray(length - j - 1));
                    }
                    break;

                default:
                    System.err.println("Precompilation: unknown "+token);
                    break;

            }
        }

        return postProcessInstructions(insns);
    }

    private int findEndOfBlock(Token start, int off, List<Token> tokens) {
        if(start.getType() != TokenType.OPENING_CURLY_BRACKETS) {
            throw new IllegalArgumentException("start");
        }
        int unclosed = 0;
        for(int i = off;i<tokens.size();i++) {
            Token t = tokens.get(i);
            switch (t.getType()) {
                case OPENING_CURLY_BRACKETS:
                    unclosed++;
                    break;

                case CLOSING_CURLY_BRACKETS:
                    unclosed--;
                    if(unclosed == 0)
                        return i;
                    break;
            }
        }
        return -1;
    }

    private List<PrecompiledInsn> postProcessInstructions(List<PrecompiledInsn> insns) {
        List<PrecompiledInsn> finalInstructions = new LinkedList<>();
        for(int i = 0;i<insns.size();i++) {
            boolean matchFound = false;
            for(InstructionPattern<PrecompiledInsn> p : patterns) {
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
        return finalInstructions;
    }

    /**
     * Implementation of <a href="https://en.wikipedia.org/wiki/Shunting-yard_algorithm">Edsger Dijkstra's Shuting-Yard Algorithm</a>
     * @param expr
     * @param tokens
     * @return
     */
    private List<Token> convertToRPN(String expr, List<Token> tokens) {
        List<Token> out = new ArrayList<>();
        Stack<Token> stack = new Stack<>();
        int argCount = 0;
        Stack<Integer> argCountStack = new Stack<>();
        VariableTopStack<Boolean> instanceStack = new VariableTopStack<>();
        instanceStack.setCurrent(false).push();
        for(int i = 0;i<tokens.size();i++) {
            Token token = tokens.get(i);
            if(token.getType() == TokenType.INSTRUCTION_END) {
                out.add(token);
            } else if(token.getType() == TokenType.NEW_LOCAL) {
                out.add(token);
            } else if(token.getType().isValue()) {
                instanceStack.setCurrent(true);
                if(i+2 < tokens.size()) {
                    if(tokens.get(i+1).getType() == TokenType.MEMBER_ACCESSING) {
                        TokenType type = tokens.get(i + 2).getType();
                        if(type == TokenType.VARIABLE || type == TokenType.THIS) {
                            out.add(token);
                            //out.add(tokens.get(i+1));
                            out.add(tokens.get(i+2));
                            argCount++;
                            i+=2;
                        } else if(type == TokenType.NULL) {
                            newError("Null has no members", -1); // todo line
                        } else { // it is a method // it is a method
                            //stack.push(tokens.get(i+1));
                            out.add(token);
                            argCount++;
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
            } else if(token.getType() == TokenType.FUNCTION || token.getType() == TokenType.IF || token.getType() == TokenType.ELSEIF) {
                out.add(new FunctionStartToken(token.getType()));
                stack.push(token);
                argCountStack.push(argCount);
                argCount = 0;
            } else if(token.getType() == TokenType.ARGUMENT_SEPARATOR) {
                out.add(token);
                instanceStack.setCurrent(false);
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
            } else if(token.getType() == TokenType.UNARY_OPERATOR || token.getType() == TokenType.BINARY_OPERATOR || token.getType() == TokenType.CAST) {
                EnumOperators operator = EnumOperators.get(token.getContent());
                if(operator == null && token.getType() == TokenType.CAST) {
                    operator = EnumOperators.CAST;
                }
                if(operator != null) {
                    if(operator != EnumOperators.RETURN && operator != EnumOperators.THROW) {
                        while (!stack.isEmpty() && (stack.peek().getType() == TokenType.UNARY_OPERATOR || stack.peek().getType() == TokenType.BINARY_OPERATOR || stack.peek().getType() == TokenType.CAST)) {
                            Token stackTop = stack.pop();
                            EnumOperators operator2 = EnumOperators.get(stackTop.getContent());
                            if(operator2 == null && stackTop.getType() == TokenType.CAST) {
                                operator2 = EnumOperators.CAST;
                            }
                            if (operator2 != null) {
                                if(operator2 == EnumOperators.RETURN || operator2 == EnumOperators.THROW) {
                                    stack.push(stackTop);
                                    break;
                                } else {
                                    if (operator.associativity() == EnumOperators.Associativity.LEFT) {
                                        if (operator.precedence() > operator2.precedence()) {
                                            out.add(stackTop);
                                            if(!operator2.isUnary()) {
                                                instanceStack.pop();
                                                argCount--;
                                                instanceStack.setCurrent(false).push();
                                            }
                                        } else {
                                            break;
                                        }
                                    } else {
                                        if (operator.precedence() >= operator2.precedence()) {
                                            out.add(stackTop);
                                            if(!operator2.isUnary()) {
                                                instanceStack.pop();
                                                argCount--;
                                                instanceStack.setCurrent(false).push();
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            } else {
                                newError("Null operator ? "+stackTop.getContent(), -1);
                                break;
                            }
                        }
                    }
                    if(token.getType() == TokenType.CAST) {
                        stack.push(token);
                    } else {
                        if(!operator.isUnary()) {
                            instanceStack.pop();
                            //argCount--;
                            instanceStack.setCurrent(false).push();
                        }
                        stack.push(new Token(operator.raw(), token.getType(), token.length));
                    }
                } else {
                    newError("Null operator ? "+token.getContent(), -1);
                }
            } else if(token.isOpeningBracketLike()) {
                instanceStack.push().setCurrent(false);
                stack.push(token);
                if(token.getType() == TokenType.OPENING_CURLY_BRACKETS) {
                    out.add(token);
                } else if(token.getType() == TokenType.OPENING_SQUARE_BRACKETS) {
                    argCountStack.push(argCount);
                    argCount = 0;
                }
            } else if(token.isClosingBracketLike()) {
                boolean shouldLookForInstance = instanceStack.pop();
                if(!stack.isEmpty()) {
                    while(!stack.peek().isOpposite(token)) {
                        Token val = stack.pop();
                        out.add(val);
                        if(val.getType() == TokenType.BINARY_OPERATOR) {
                            instanceStack.pop();
                            argCount--;
                            instanceStack.setCurrent(false).push();
                        }
                    }

                    if(stack.isEmpty()) {
                        newError("Unmatched parenthesises0, please fix in "+expr+" / "+Arrays.toString(tokens.toArray()), -1);
                        return Collections.EMPTY_LIST;
                    }

                    Token previous = stack.pop(); // pop opening bracket
                    if(token.getType() == TokenType.CLOSING_SQUARE_BRACKETS) {
                        out.add(new Token(""+argCount, TokenType.DEFINE_ARRAY, -1));
                        argCount = argCountStack.pop();
                        argCount++;
                    } else {
                        if(previous.getType() == TokenType.OPENING_SQUARE_BRACKETS) {
                            argCount++;
                        } else {
                            if(!stack.isEmpty()) {
                                Token top = stack.peek();
                                if(top.getType() == TokenType.FUNCTION || top.getType() == TokenType.IF || top.getType() == TokenType.ELSEIF) {
                                    Token originalToken = stack.pop();
                                    /*if(!stack.isEmpty()) {
                                        if(stack.peek().getType() == TokenType.MEMBER_ACCESSING) {
                                            shouldLookForInstance = true;
                                            stack.pop();
                                        }
                                    } else if(stack.isEmpty() && out.size()-argCount > 0) {
                                        shouldLookForInstance = true;
                                    }*/
                                    /*for(int j = 0;j<argCount;i++)
                                        instanceStack.pop();*/
                                    // function name;argument count;true if we should look for the object to call it on in the stack
                                    Token functionToken = new Token(originalToken.getContent()+";"+argCount+";"+String.valueOf(shouldLookForInstance), top.getType(), originalToken.length);
                                    argCount = argCountStack.pop();
                                    if(!shouldLookForInstance)
                                        argCount++;
                                    out.add(functionToken);
                                }
                            }
                        }

                        if(token.getType() == TokenType.CLOSING_CURLY_BRACKETS) {
                            out.add(token);
                        }
                    }
                } else {
                    newError("Unmatched parenthesises, please fix in "+expr, -1);
                    return Collections.EMPTY_LIST;
                }
                instanceStack.setCurrent(true);
            } else if(token.getType() == TokenType.ELSE) {
                out.add(token);
            }
        }
        while(!stack.isEmpty()) {
            Token token = stack.pop();
            if(token.isOpeningBracketLike()) {
                newError("Unmatched parenthesis in "+expr+", found "+token+" instead of opening parenthesis", -1);
                break;
            }
            out.add(token);
        }
        return out;
    }

    @Override
    public Class<ParsedSource> getInputClass() {
        return ParsedSource.class;
    }

    @Override
    public Class<PrecompiledSource> getOutputClass() {
        return PrecompiledSource.class;
    }
}
