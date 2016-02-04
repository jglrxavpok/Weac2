package org.jglr.weac.precompile;

import org.jglr.weac.WeacCompilePhase;
import org.jglr.weac.parse.structure.*;
import org.jglr.weac.patterns.WeacInstructionPattern;
import org.jglr.weac.precompile.insn.*;
import org.jglr.weac.precompile.patterns.WeacIntervalPattern;
import org.jglr.weac.precompile.structure.*;
import org.jglr.weac.utils.EnumOperators;
import org.jglr.weac.utils.Identifier;

import java.util.*;

public class WeacPreCompiler extends WeacCompilePhase<WeacParsedSource, WeacPrecompiledSource> {

    public static final char[] extraDigits = (
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ"+
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase()+
            "-_"
    ).toCharArray();

    private final List<WeacInstructionPattern<WeacPrecompiledInsn>> patterns;

    public WeacPreCompiler() {
        patterns = new ArrayList<>();
        patterns.add(new WeacIntervalPattern());
    }

    @Override
    public WeacPrecompiledSource process(WeacParsedSource parsed) {
        WeacPrecompiledSource source = new WeacPrecompiledSource();

        source.classes = new ArrayList<>();

        source.imports = parsed.imports;

        source.packageName = parsed.packageName;

        parsed.classes.forEach(c -> source.classes.add(precompile(c)));

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
                int offset = handleBuiltinMethod(buffer.toString(), chars, i, instructions, currentBlock, previousBlock);
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

    private int handleBuiltinMethod(String bufferContent, char[] chars, int index, List<List<WeacPrecompiledInsn>> instructions, WeacCodeBlock codeBlock, WeacCodeBlock previousBlock) {
        String name = bufferContent.replace(" ", "");
        int start = index;
        switch (name) {
            case "if":
                index++;
                String args = readArguments(chars, index);
                String condition = readSingleArgument(args, 0, false);
                List<WeacPrecompiledInsn> insns = precompileExpression(condition);

                index += args.length()+1;
                index += readUntilNot(chars, index).length();
                // read code
                if(chars[index] == '{') {
                    index++;
                    String codeBlockSource = readCodeblock(chars, index);
                    index += codeBlockSource.length()+1;

                    insns.add(new WeacLabelInsn());

                    WeacCodeBlock conditionalBlock = compileCodeBlock(codeBlockSource);
                    insns.add(new WeacBlockInsn(conditionalBlock));

                    insns.add(new WeacIfNotJumpInsn(conditionalBlock.getEnd()));
                } else {
                    newError("Invalid code block after if conditional", -1);
                }
                return index-start;
        }
        return 0;
    }

    private List<WeacPrecompiledInsn> compileSingleInstruction(String instructionSource) {
        List<WeacPrecompiledInsn> instruction = new LinkedList<>();

        return instruction;
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
            WeacToken token = readToken(chars, i);
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
            if (token.getType() == WeacTokenType.WAITING_FOR_NEXT) {
                iterator.remove();
            }
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
        List<WeacToken> output = convertToRPN(expression, tokens);
        output.forEach(t -> {
            if(t.getType() == WeacTokenType.VARIABLE || t.getType() == WeacTokenType.LITERAL) {
                switch (t.getContent()) {
                    case "true":
                    case "false":
                        t.setType(WeacTokenType.BOOLEAN);
                        break;

                    case "this":
                        t.setType(WeacTokenType.THIS);
                        break;
                }
            }
        });

        for(WeacToken token : output) {
            System.out.print(token.getType().name()+"("+token.getContent()+") ");
        }
        System.out.println();
        return toInstructions(output, insns);
    }

    private List<WeacPrecompiledInsn> toInstructions(List<WeacToken> output, List<WeacPrecompiledInsn> insns) {
        // TODO: Handle 'new' after function calls
        for(WeacToken token : output) {
            switch (token.getType()) {
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

                case UNARY_OPERATOR:
                    EnumOperators operator = EnumOperators.get(token.getContent(), true);
                    if(operator == EnumOperators.RETURN) {
                        insns.add(new WeacSimplePreInsn(PrecompileOpcodes.RETURN));
                    } else {
                        insns.add(new WeacOperatorInsn(operator));
                    }
                    break;

                case DEFINE_ARRAY:
                    int length = Integer.parseInt(token.getContent());
                    insns.add(new WeacCreateArray(length, "unknown"));
                    for(int i = 0;i<length;i++)
                        insns.add(new WeacStoreArray(length-i-1));
                    break;

            }
        }

        return postProcessInstructions(insns);
    }

    private List<WeacPrecompiledInsn> postProcessInstructions(List<WeacPrecompiledInsn> insns) {
        List<WeacPrecompiledInsn> finalInstructions = new LinkedList<>();
        for(int i = 0;i<insns.size();i++) {
            boolean matchFound = false;
            for(WeacInstructionPattern<WeacPrecompiledInsn> p : patterns) {
                if(p.matches(insns, i)) {
                    p.output(insns, i, finalInstructions);
                    i += p.consumeCount(insns, i);
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
    private List<WeacToken> convertToRPN(String expr, List<WeacToken> tokens) {
        List<WeacToken> out = new ArrayList<>();
        Stack<WeacToken> stack = new Stack<>();
        int argCount = 0;
        Stack<Integer> argCountStack = new Stack<>();
        for(int i = 0;i<tokens.size();i++) {
            WeacToken token = tokens.get(i);
            if(token.getType() == WeacTokenType.NUMBER || token.getType() == WeacTokenType.STRING
                    || token.getType() == WeacTokenType.SINGLE_CHARACTER || token.getType() == WeacTokenType.VARIABLE) {
                if(i+2 < tokens.size()) {
                    if(tokens.get(i+1).getType() == WeacTokenType.MEMBER_ACCESSING) {
                        if(tokens.get(i+2).getType() == WeacTokenType.VARIABLE) {
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
            } else if(token.getType() == WeacTokenType.FUNCTION) {
                stack.push(token);
                argCountStack.push(argCount);
                argCount = 0;
            } else if(token.getType() == WeacTokenType.ARGUMENT_SEPARATOR) {
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
                if(token.getType() == WeacTokenType.OPENING_SQUARE_BRACKETS) {
                    argCountStack.push(argCount);
                    argCount = 0;
                }
            } else if(token.isClosingBracketLike()) {
                if(!stack.isEmpty()) {
                    while(!stack.peek().isOpposite(token)) {
                        if(stack.peek().getType() == WeacTokenType.BINARY_OPERATOR || stack.peek().getType() == WeacTokenType.UNARY_OPERATOR) {
                            out.add(stack.pop());
                        } else {
                            break;
                        }
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
                                    if(top.getType() == WeacTokenType.FUNCTION) {
                                        WeacToken originalToken = stack.pop();
                                        boolean shouldLookForInstance = false;
                                        if(!stack.isEmpty()) {
                                            if(stack.peek().getType() == WeacTokenType.MEMBER_ACCESSING) {
                                                shouldLookForInstance = true;
                                                stack.pop();
                                            }
                                        } else if(stack.isEmpty() && out.size()-argCount > 0) {
                                            shouldLookForInstance = true;
                                        }
                                        // function name;argument count;true if we should look for the object to call it on in the stack
                                        WeacToken functionToken = new WeacToken(originalToken.getContent()+";"+argCount+";"+String.valueOf(shouldLookForInstance), WeacTokenType.FUNCTION, originalToken.length);
                                        argCount = argCountStack.pop();
                                        argCount++;
                                        out.add(functionToken);

                                    }
                                }
                            }
                        }
                    }
                } else {
                    newError("Unmatched parenthesises, please fix", -1);
                    return Collections.EMPTY_LIST;
                }
            }
        }
        while(!stack.isEmpty()) {
            WeacToken token = stack.pop();
            if(token.isOpeningBracketLike()) {
                newError("Unmatched parenthesis in "+expr, -1);
                break;
            }
            out.add(token);
        }
        return out;
    }

    private WeacToken readToken(char[] chars, int i) {
        if (i >= chars.length) {
            return null;
        } else {
            char first = chars[i];
            if(Character.isDigit(first)) {
                // it's a number, read it!
                String number = readNumber(chars, i);
                return new WeacToken(number, WeacTokenType.NUMBER, number.length());
            } else {
                if (first == '.') {
                    if (i + 1 < chars.length) {
                        char next = chars[i + 1];
                        if (Character.isDigit(next)) {
                            String number = readNumber(chars, i + 1);
                            return new WeacToken(number, WeacTokenType.NUMBER, number.length());
                        } else if(next == '.') {
                            return new WeacToken("..", WeacTokenType.BINARY_OPERATOR, 2);
                        }
                    }
                    return new WeacToken(String.valueOf(first), WeacTokenType.MEMBER_ACCESSING, 1);
                }
                switch (first) {
                    case ':':
                        return new WeacToken(String.valueOf(first), WeacTokenType.INTERVAL_STEP, 1);

                    case ' ':
                        return new SpaceToken();

                    case '\'':
                        // read character
                        // is naming it Chara an Undertale reference? I don't know... Am I allowed to do it?
                        String chara = readCharacter(chars, i);
                        if (chara != null) {
                            return new WeacToken(chara, WeacTokenType.SINGLE_CHARACTER, chara.length() + 2);
                        } else {
                            newError("Invalid character", -1); // TODO: find line
                            return null;
                        }

                    case '"':
                        String text = readString(chars, i);
                        if (text != null) {
                            return new WeacToken(text, WeacTokenType.STRING, text.length() + 2);
                        } else {
                            newError("Invalid string constant", -1); // TODO: find line
                            return null;
                        }

                    case '[':
                        return new WeacToken(String.valueOf(first), WeacTokenType.OPENING_SQUARE_BRACKETS, 1);

                    case ']':
                        return new WeacToken(String.valueOf(first), WeacTokenType.CLOSING_SQUARE_BRACKETS, 1);

                    case '(':
                        return new WeacToken(String.valueOf(first), WeacTokenType.OPENING_PARENTHESIS, 1);

                    case ')':
                        return new WeacToken(String.valueOf(first), WeacTokenType.CLOSING_PARENTHESIS, 1);

                    case '{':
                        return new WeacToken(String.valueOf(first), WeacTokenType.OPENING_CURLY_BRACKETS, 1);

                    case '}':
                        return new WeacToken(String.valueOf(first), WeacTokenType.CLOSING_CURLY_BRACKETS, 1);

                    case ',':
                        return new WeacToken(String.valueOf(first), WeacTokenType.ARGUMENT_SEPARATOR, 1);

                }

                String literal = readLiteral(chars, i);
                if(literal.isEmpty()) {
                    String operator = readOperator(chars, i);
                    if(operator != null && !operator.isEmpty())
                        return new WeacToken(operator, WeacTokenType.OPERATOR, operator.length());
                } else {
                    if(literal.isEmpty())
                        return null;
                    // check if we did not read an operator by mistake
                    EnumOperators potentialOperator = EnumOperators.get(literal, false);
                    if(potentialOperator != null) {
                        return new WeacToken(potentialOperator.raw(), WeacTokenType.OPERATOR, potentialOperator.raw().length());
                    } else {
                        return new WeacToken(literal, WeacTokenType.LITERAL, literal.length());
                    }
                }
                return null;
            }
        }
    }

    private String readLiteral(char[] chars, int i) {
        return Identifier.read(chars, i).getId();
    }

    private String readOperator(char[] chars, int offset) {
        List<EnumOperators> operators = new LinkedList<>();
        Collections.addAll(operators, EnumOperators.values());
        operators.remove(EnumOperators.UNARY_MINUS);
        operators.remove(EnumOperators.UNARY_PLUS);
        for(int i = offset;i<chars.length;i++) {
            char c = chars[i];
            int localIndex = i-offset;
            Iterator<EnumOperators> iterator = operators.iterator();
            while(iterator.hasNext()) {
                EnumOperators operator = iterator.next();
                if(operator.raw().length()+offset >= chars.length) {
                    iterator.remove();
                } else if(localIndex < operator.raw().length() && operator.raw().charAt(localIndex) != c) {
                    iterator.remove();
                } else if(localIndex > operator.raw().length()) {
                    iterator.remove();
                }
            }

            if(operators.size() == 1) {
                return operators.get(0).raw();
            }
        }
        return null;
    }

    private String readInQuotes(char[] chars, int offset, char quote) {
        char first = chars[offset];
        if(first == quote) {
            boolean escaped = false;
            StringBuilder builder = new StringBuilder();
            for(int i = offset+1;i<chars.length;i++) {
                char c = chars[i];
                if(c == quote && !escaped) {
                    break;
                } else if(c == '\\') {
                    if(!escaped) {
                        escaped = true;
                        continue;
                    } else {
                        builder.append('\\');
                    }
                } else {
                    if(escaped) {
                        escaped = false;
                        builder.append('\\');
                    }
                    builder.append(c);
                }
            }
            return builder.toString();
        }
        return null;
    }

    private String readString(char[] chars, int offset) {
        return readInQuotes(chars, offset, '"');
    }

    private String readCharacter(char[] chars, int offset) {
        return readInQuotes(chars, offset, '\'');
    }

    private String readNumber(char[] chars, int offset) {
        StringBuilder buffer = new StringBuilder();
        boolean hasDecimalPoint = false;
        boolean canHaveCustomBase = false;
        if(chars[offset] == '0') {
            canHaveCustomBase = true;
        }
        int base = 10;
        for(int i = offset;i<chars.length;i++) {
            char c = chars[i];
            if(isDigit(c, base)) {
                buffer.append(c);
            } else {
                if(isBaseCharacter(c) && i == offset+1 && canHaveCustomBase) {
                    buffer.append(c);
                    base = getBase(c);
                    if(base == -10) {
                        String baseStr = readBase(chars, i+1);
                        i+=baseStr.length()+1;
                        base = Integer.parseInt(baseStr);
                        buffer.append(baseStr).append("#");
                    }

                    if(base < 0) {
                        newError("Invalid number base: "+c, -1);
                        break;
                    }
                } else if(c == '.') {
                    if(!hasDecimalPoint) {
                        if(i+1 < chars.length) {
                            char next = chars[i+1];
                            if(next == '.') {
                                break;
                            }
                        }
                        buffer.append(c);
                        hasDecimalPoint = true;
                    } else {
                        break;
                    }
                } else {
                    switch (c) {
                        case 'f': // float
                        case 'F': // float
                        case 'd': // double
                        case 'D': // double
                        case 'l': // long
                        case 'L': // long
                        case 's': // short
                        case 'S': // short
                        case 'b': // byte
                        case 'B': // byte
                            buffer.append(c);
                            break;
                    }
                    break;
                }
            }
        }
        return buffer.toString();
    }

    private String readBase(char[] chars, int offset) {
        StringBuilder builder = new StringBuilder();
        for(int i = offset;i<chars.length;i++) {
            char c = chars[i];
            if(c == '#') {
                break;
            } else if(isDigit(c, 10)) {
                builder.append(c);
            } else {
                newError("Invalid base character: "+c, -1); // TODO: find correct line
                break;
            }
        }
        return builder.toString();
    }

    private boolean isDigit(char c, int base) {
        if (base == 10) {
            return Character.isDigit(c);
        } else if(base < 10) {
            return Character.isDigit(c) && (c-'0') < base;
        } else {
            // ((c-'A')+10 < base) checks if the character is a valid digit character
            return Character.isDigit(c) || ((c-'A')+10 < base && (c-'A')+10 > 0);
        }
    }

    private int getBase(char c) {
        switch (c) {
            case 'x':
                return 16;

            case 'b':
                return 2;

            case 'o':
                return 8;

            case 'c':
                return -10;
        }
        return -1;
    }

    private boolean isBaseCharacter(char c) {
        switch (c) {
            case 'x':
            case 'b':
            case 'o':
                return true;

            case 'c':
                return true;
        }
        return false;
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
