package weac.compiler.precompile;

import weac.compiler.CompileUtils;
import weac.compiler.parser.Parser;
import weac.compiler.utils.EnumOperators;
import weac.compiler.utils.Identifier;

public class Tokenizer extends CompileUtils {

    public Token nextToken(Parser parser) {
        if (parser.hasReachedEnd()) {
            return null;
        } else {
            char first = parser.nextCharacter();
            if(Character.isDigit(first)) {
                // it's a number, read it!
                parser.backwards(1);
                String number = readNumber(parser);
                return new Token(number, TokenType.NUMBER, number.length());
            } else {
                if (first == '.') {
                    parser.backwards(1);
                    if(parser.isAt("..")) {
                        parser.forward(2);
                        return new Token("..", TokenType.BINARY_OPERATOR, 2);
                    } else if(Character.isDigit(parser.getCurrentCharacter())) {
                        parser.backwards(1);
                        String number = readNumber(parser);
                        parser.discardMark();
                        return new Token(number, TokenType.NUMBER, number.length());
                    }
                    parser.forward(1);
                    return new Token(String.valueOf(first), TokenType.MEMBER_ACCESSING, 1);
                }
                switch (first) {
                    case '\n':
                        return new NewLineToken(); // TODO: Handle linenumbers properly

                    case ':':
                        return new Token(String.valueOf(first), TokenType.INTERVAL_STEP, 1);

                    case ' ':
                        return new SpaceToken();

                    case '\'':
                        // read character
                        String chara = readCharacter(parser);
                        if (chara != null) {
                            return new Token(chara, TokenType.SINGLE_CHARACTER, chara.length() + 2);
                        } else {
                            newError("Invalid character", -1); // TODO: find line
                            return null;
                        }

                    case '"':
                        String text = readString(parser);
                        if (text != null) {
                            return new Token(text, TokenType.STRING, text.length() + 2);
                        } else {
                            newError("Invalid string constant", -1); // TODO: find line
                            return null;
                        }

                    case '[':
                        return new Token(String.valueOf(first), TokenType.OPENING_SQUARE_BRACKETS, 1);

                    case ']':
                        return new Token(String.valueOf(first), TokenType.CLOSING_SQUARE_BRACKETS, 1);

                    case '(':
                        return new Token(String.valueOf(first), TokenType.OPENING_PARENTHESIS, 1);

                    case ')':
                        return new Token(String.valueOf(first), TokenType.CLOSING_PARENTHESIS, 1);

                    case '{':
                        return new Token(String.valueOf(first), TokenType.OPENING_CURLY_BRACKETS, 1);

                    case '}':
                        return new Token(String.valueOf(first), TokenType.CLOSING_CURLY_BRACKETS, 1);

                    case ',':
                        return new Token(String.valueOf(first), TokenType.ARGUMENT_SEPARATOR, 1);

                    case ';':
                        return new Token(String.valueOf(first), TokenType.INSTRUCTION_END, 1);

                    case '/':
                        if(parser.isAt("/")) {
                            String comment = parser.forwardToOrEnd("\n");
                            if(!parser.hasReachedEnd())
                                parser.forward(1);
                            return new CommentToken(comment, false);
                        } else if(parser.isAt("*")) {
                            String comment = parser.forwardToOrEnd("*/");
                            if(!parser.hasReachedEnd())
                                parser.forward(2);
                            return new CommentToken(comment, false);
                        }
                }
                parser.backwards(1);

                String literal = readLiteral(parser);
                if(literal.equals("native")) {
                    parser.forwardTo("{");
                    parser.forward(1);
                    String nativeCode = parser.forwardTo("}");
                    parser.forward(1);
                    return new NativeCodeToken(nativeCode, TokenType.NATIVE_CODE, nativeCode.length());
                }
                if(literal.isEmpty()) {
                    String operator = readOperator(parser);
                    if(operator != null && !operator.isEmpty()) {
                        return new Token(operator, TokenType.OPERATOR, operator.length());
                    }
                } else {
                    if(literal.isEmpty())
                        return null;
                    // check if we did not read an operator by mistake
                    EnumOperators potentialOperator = EnumOperators.get(literal);
                    if(potentialOperator != null) {
                        return new Token(potentialOperator.raw(), TokenType.OPERATOR, potentialOperator.raw().length());
                    } else {
                        return new Token(literal, TokenType.LITERAL, literal.length());
                    }
                }
                return null;
            }
        }
    }

    private String readLiteral(Parser parser) {
        return Identifier.read(parser).getId();
    }

    private String readInQuotes(Parser parser, String quote) {
        String read = parser.forwardTo(quote);
        parser.forward(quote.length()); // skip closing character(s)
        return read;
    }

    private String readString(Parser parser) {
        return readInQuotes(parser, "\"");
    }

    private String readCharacter(Parser parser) {
        return readInQuotes(parser, "'");
    }

    private String readNumber(Parser parser) {
        StringBuilder buffer = new StringBuilder();
        boolean hasDecimalPoint = false;
        boolean canHaveCustomBase = false;
        if(parser.isAt("0")) {
            canHaveCustomBase = true;
        }
        int base = 10;
        parser.mark();
        while(!parser.hasReachedEnd()) {
            char c = parser.getCurrentCharacter();
            if(isDigit(c, base)) {
                buffer.append(c);
            } else {
                if(isBaseCharacter(c) && parser.distanceFromMark() == 1 && canHaveCustomBase) {
                    buffer.append(c);
                    base = getBase(c);
                    if(base == -10) {
                        parser.forward(1);
                        String baseStr = readBase(parser);
                        //parser.forward(1);
                        base = Integer.parseInt(baseStr);
                        buffer.append(baseStr).append("#");
                    }

                    if(base < 0) {
                        newError("Invalid number base: "+c, -1);
                        break;
                    }
                } else if(c == '.') {
                    if(!hasDecimalPoint && base == 10) {
                        if(parser.getPosition()+1 < parser.getDataSize()) {
                            char next = parser.getRelativeCharacter(1);
                            if(next == '.') {
                                break;
                            }
                            parser.forward(1);
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
                            parser.forward(1);
                            break;
                    }
                    break;
                }
            }
            parser.forward(1);
        }
        parser.discardMark();
        return buffer.toString();
    }

    private String readBase(Parser parser) {
        StringBuilder builder = new StringBuilder();
        while(!parser.hasReachedEnd()) {
            if(parser.isAt("#")) {
                break;
            } else {
                char c = parser.nextCharacter();
                if(isDigit(c, 10)) {
                    builder.append(c);
                } else {
                    newError("Invalid base character: "+c, -1); // TODO: find correct line
                    break;
                }
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
