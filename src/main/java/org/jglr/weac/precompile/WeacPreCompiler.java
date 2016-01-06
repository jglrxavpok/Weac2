package org.jglr.weac.precompile;

import org.jglr.weac.WeacCompilePhase;
import org.jglr.weac.parse.structure.WeacParsedClass;
import org.jglr.weac.parse.structure.WeacParsedField;
import org.jglr.weac.parse.structure.WeacParsedMethod;
import org.jglr.weac.parse.structure.WeacParsedSource;
import org.jglr.weac.precompile.insn.WeacPrecompiledInsn;
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

    @Override
    public WeacPrecompiledSource process(WeacParsedSource parsed) {
        WeacPrecompiledSource source = new WeacPrecompiledSource();

        source.imports = parsed.imports;

        source.packageName = parsed.packageName;

        parsed.classes.forEach(c -> source.classes.add(precompile(c)));

        return source;
    }

    private WeacPrecompiledClass precompile(WeacParsedClass c) {
        WeacPrecompiledClass clazz = new WeacPrecompiledClass();
        clazz.access = c.access;
        clazz.annotations = c.annotations;
        clazz.classType = c.classType;
        clazz.enumConstants = precompileEnumConstants(c.enumConstants);
        clazz.fields = precompileFields(c.fields);
        clazz.interfacesImplemented = c.interfacesImplemented;
        clazz.isAbstract = c.isAbstract;
        clazz.isMixin = c.isMixin;
        clazz.methods = precompileMethods(c.methods);
        clazz.motherClass = c.motherClass;
        clazz.name = c.name;
        return clazz;
    }

    private List<WeacPrecompiledMethod> precompileMethods(List<WeacParsedMethod> methods) {
        return null;
    }

    private List<WeacPrecompiledField> precompileFields(List<WeacParsedField> fields) {
        return null;
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
        List<WeacPrecompiledInsn> insns = new LinkedList<>();
        char[] chars = expression.toCharArray();
        List<WeacToken> tokens = new LinkedList<>();
        for(int i = 0;i<chars.length;) {
            char c = chars[i];
            WeacToken token = readToken(chars, i);
            if(token != null) {
                i += token.length;
                tokens.add(token);
            } else {
                break; // reached end of file
            }
        }
        Iterator<WeacToken> iterator = tokens.iterator();
        WeacToken previous = null;
        while(iterator.hasNext()) {
            WeacToken token = iterator.next();
            if(token.getType() == WeacTokenType.WAITING_FOR_NEXT) {
                iterator.remove();
            } else if(previous != null) {
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
            previous = token;
        }
        for(WeacToken token : tokens) {
            System.out.print(token.getType().name()+"("+token.getContent()+") ");
        }
        System.out.println();
        // TODO: reserve polish notates everything
        return insns;
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
                if(first == '.') {
                    if(i+1 < chars.length) {
                        char next = chars[i+1];
                        if(Character.isDigit(next)) {
                            String number = readNumber(chars, i+1);
                            return new WeacToken(number, WeacTokenType.NUMBER, number.length());
                        } else if(next == '.') {
                            return new WeacToken("..", WeacTokenType.INTERVAL_SEPARATOR, 2);
                        }
                    }
                    return new WeacToken(String.valueOf(first), WeacTokenType.MEMBER_ACCESSING, 1);
                }
                switch(first) {
                    case ':':
                        return new WeacToken(String.valueOf(first), WeacTokenType.INTERVAL_STEP, 1);

                    case ' ':
                        return new SpaceToken();

                    case '\'':
                        // read character

                        // is naming it Chara an Undertale reference? I don't know... Am I allowed to do it?
                        String chara = readCharacter(chars, i);
                        if(chara != null) {
                            return new WeacToken(chara, WeacTokenType.SINGLE_CHARACTER, chara.length()+2);
                        } else {
                            newError("Invalid character", -1); // TODO: find line
                            return null;
                        }

                    case '"':
                        String text = readString(chars, i);
                        if(text != null) {
                            return new WeacToken(text, WeacTokenType.STRING, text.length()+2);
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

                String operator = readOperator(chars, i);
                if(operator != null) {
                    return new WeacToken(operator, WeacTokenType.OPERATOR, operator.length());
                } else {
                    // read literal
                    String literal = readLiteral(chars, i);
                    return new WeacToken(literal, WeacTokenType.LITERAL, literal.length());
                }
            }
        }
    }

    private String readLiteral(char[] chars, int i) {
        return Identifier.read(chars, i).getId();
    }

    private String readOperator(char[] chars, int offset) {
        List<EnumOperators> operators = new LinkedList<>();
        Collections.addAll(operators, EnumOperators.values());
        for(int i = offset;i<chars.length;i++) {
            char c = chars[i];
            int localIndex = i-offset;
            Iterator<EnumOperators> iterator = operators.iterator();
            while(iterator.hasNext()) {
                EnumOperators operator = iterator.next();
                if(localIndex < operator.raw().length() && operator.raw().charAt(localIndex) != c) {
                    iterator.remove();
                }
                if(localIndex > operator.raw().length())
                    iterator.remove();
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
                    builder.append(c);
                }

                if(escaped) {
                    escaped = false;
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
