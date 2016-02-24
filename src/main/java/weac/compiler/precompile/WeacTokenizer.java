package weac.compiler.precompile;

import weac.compiler.WeacCompileUtils;
import weac.compiler.utils.EnumOperators;
import weac.compiler.utils.Identifier;

public class WeacTokenizer extends WeacCompileUtils {

    public WeacToken nextToken(char[] chars, int offset) {
        if (offset >= chars.length) {
            return null;
        } else {
            char first = chars[offset];
            if(Character.isDigit(first)) {
                // it's a number, read it!
                String number = readNumber(chars, offset);
                return new WeacToken(number, WeacTokenType.NUMBER, number.length());
            } else {
                if (first == '.') {
                    if (offset + 1 < chars.length) {
                        char next = chars[offset + 1];
                        if (Character.isDigit(next)) {
                            String number = readNumber(chars, offset + 1);
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
                        String chara = readCharacter(chars, offset);
                        if (chara != null) {
                            return new WeacToken(chara, WeacTokenType.SINGLE_CHARACTER, chara.length() + 2);
                        } else {
                            newError("Invalid character", -1); // TODO: find line
                            return null;
                        }

                    case '"':
                        String text = readString(chars, offset);
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

                String literal = readLiteral(chars, offset);
                if(literal.isEmpty()) {
                    String operator = readOperator(chars, offset);
                    if(operator != null && !operator.isEmpty())
                        return new WeacToken(operator, WeacTokenType.OPERATOR, operator.length());
                } else {
                    if(literal.isEmpty())
                        return null;
                    // check if we did not read an operator by mistake
                    EnumOperators potentialOperator = EnumOperators.get(literal);
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
                    if(!hasDecimalPoint && base == 10) {
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
}
