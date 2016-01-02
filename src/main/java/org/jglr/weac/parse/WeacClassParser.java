package org.jglr.weac.parse;

import org.jglr.weac.WeacCompileUtils;
import org.jglr.weac.parse.structure.WeacParsedClass;
import org.jglr.weac.parse.structure.WeacParsedField;
import org.jglr.weac.parse.structure.WeacParsedMethod;
import org.jglr.weac.utils.Identifier;
import org.jglr.weac.utils.WeacModifier;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Parses a class source to find its components: hierarchy, fields, and methods.
 */
public class WeacClassParser extends WeacCompileUtils {

    public WeacClassParser() {

    }

    /**
     * Parses a single class from the given source
     * @param source
     *              The class code
     * @param startingLine
     *              The line at which the class starts in the source file
     * @return
     *              The class extracted from the source file
     */
    public WeacParsedClass parseClass(String source, int startingLine) {
        WeacParsedClass parsedClass = new WeacParsedClass();
        parsedClass.startingLine = startingLine;
        parsedClass.enumConstants = new ArrayList<>();
        parsedClass.fields = new ArrayList<>();
        parsedClass.methods = new ArrayList<>();
        parsedClass.interfacesImplemented = new ArrayList<>();
        String header = source.substring(0, source.indexOf('{'));
        parseHeader(parsedClass, header);
        String body = source.substring(source.indexOf('{')+1, source.length()-1);
        parseBody(parsedClass, body, startingLine);
        return parsedClass;
    }

    /**
     * Parses the body of the class. Locates methods and fields
     * @param parsedClass
     *                  The current class
     * @param body
     *                  The body code
     * @param startingLine
     *                  The line at which the class starts in the source file
     */
    private void parseBody(WeacParsedClass parsedClass, String body, int startingLine) {
        body = trimStartingSpace(body);
        char[] chars = body.toCharArray();
        int lineIndex = 0;
        List<WeacModifier> modifiers = new LinkedList<>();
        for(int i = 0;i<chars.length;i++) {
            char c = chars[i];
            if(c == '\n') {
                lineIndex++;
            }
            i += readModifiers(chars, i, modifiers);
            WeacModifier currentAccess = null;
            for(WeacModifier modif : modifiers) {
                // TODO: Abstract & mixins
                if(modif.isAccessModifier()) {
                    if(currentAccess != null) {
                        newError("Cannot specify twice the access permissions", lineIndex+startingLine);
                    } else {
                        currentAccess = modif;
                    }
                }
            }
            modifiers.clear();
            if(currentAccess == null)
                currentAccess = WeacModifier.PUBLIC;
            i += readUntilNot(chars, i, ' ').length();
            if(i >= chars.length)
                break;
            if(parsedClass.classType == EnumClassTypes.ENUM) {
                String constants = readUntilInsnEnd(chars, i);
                // TODO: Read Enum constants

                i += constants.length()+1;
                i += readUntilNot(chars, i, ' ', '\n').length();
                System.out.println(">> "+constants);

                fillEnumConstants(constants, parsedClass.enumConstants);

                if(i >= chars.length) // We might have reached end of file
                    break;
            }
            Identifier firstPart = Identifier.read(chars, i);
            if(firstPart.isValid()) {
                i += firstPart.getId().length();
                i += readUntilNot(chars, i, ' ', '\n').length();
                if(chars[i] == '(') { // Constructor

                    WeacParsedMethod function = readFunction(chars, i, Identifier.VOID, firstPart, currentAccess);
                    function.startingLine = lineIndex+startingLine;
                    parsedClass.methods.add(function);
                    i += function.off;

                } else {
                    Identifier secondPart = Identifier.read(chars, i);
                    i+=secondPart.getId().length();
                    if(!secondPart.isValid()) {
                        newError("Invalid identifier: "+secondPart.getId(), startingLine);
                    } else {
                        int potentialFunction = indexOf(chars, i, '{');
                        int potentialField = indexOf(chars, i, ';');
                        int nameEnd;
                        boolean isField = false; // true for field, false for function
                        if(potentialField == -1) {
                            nameEnd = potentialFunction;
                        } else if(potentialFunction == -1) {
                            nameEnd = potentialField;
                            isField = true;
                        } else {
                            if(potentialField < potentialFunction) {
                                nameEnd = potentialField;
                                isField = true;
                            } else {
                                nameEnd = potentialFunction;
                            }
                        }

                        String start = read(chars, i, nameEnd);
                        if(isField) {
                            WeacParsedField field = new WeacParsedField();
                            field.name = secondPart;
                            field.type = firstPart;
                            field.access = currentAccess;
                            field.startingLine = startingLine+lineIndex;
                            if(start.contains("=")) {
                                field.defaultValue = trimStartingSpace(start.split("=")[1]);
                            }
                            parsedClass.fields.add(field);
                            i = nameEnd+1;
                        } else {
                            WeacParsedMethod function = readFunction(chars, i, firstPart, secondPart, currentAccess);
                            function.startingLine = lineIndex+startingLine;
                            parsedClass.methods.add(function);
                            i += function.off;
                        }
                    }
                }
            } else {
                newError("Invalid identifier: " + firstPart.getId(), startingLine+lineIndex);
            }
        }
    }

    private void fillEnumConstants(String constantList, List<String> out) {
        int i = 0;
        String constant = readSingleArgument(constantList, 0, true).replace("\n", "");
        while(!constant.isEmpty()) {
            out.add(constant);

            i += constant.length()+1;

            i += readUntilNot(constantList.toCharArray(), i, ' ', '\n', ',', '\r').length();
            constant = readSingleArgument(constantList, i, true).replace("\n", "");
        }
    }

    private String readSingleArgument(String constantList, int offset, boolean isSemiColonValidSeparator) {
        StringBuilder builder = new StringBuilder();
        boolean inString = false;
        boolean inQuote = false;
        boolean escaped = false;
        int unclosedCurlyBrackets = 0;
        int unclosedBrackets = 0;
        char[] chars = constantList.toCharArray();
        iterationLoop: for(int i = offset; i<chars.length;i++) {
            char c = chars[i];
            boolean append = true;
            switch (c) {
                case '"':
                    if (!inQuote && !escaped)
                        inString = !inString;
                    break;

                case '\'':
                    if (!inString && !escaped)
                        inQuote = !inQuote;
                    break;

                case '\\':
                    if(!escaped) {
                        append = false;
                        escaped = true;
                    }
                    break;

                case '(':
                    unclosedBrackets++;
                    break;

                case ')':
                    unclosedBrackets--;
                    break;

                case '{':
                    unclosedCurlyBrackets++;
                    break;

                case '}':
                    unclosedCurlyBrackets--;
                    break;

                case ',':
                    if(unclosedCurlyBrackets == 0 && unclosedBrackets == 0) {
                        break iterationLoop;
                    }
                    break;

                case ';':
                    if(isSemiColonValidSeparator && unclosedCurlyBrackets == 0 && unclosedBrackets == 0) {
                        break iterationLoop;
                    }
                    break;
            }
            if (append)
                builder.append(c);
        }
        return builder.toString();
    }

    private String readUntilInsnEnd(char[] chars, int offset) {
        StringBuilder builder = new StringBuilder();

        boolean inString = false;
        boolean inQuote = false;
        boolean escaped = false;
        finalLoop: for(int i = offset;i<chars.length;i++) {
            char c = chars[i];
            boolean append = true;
            switch (c) {
                case '"':
                    if(!inQuote && !escaped)
                        inString = !inString;
                    break;

                case '\'':
                    if(!inString && !escaped)
                        inQuote = !inQuote;
                    break;

                case '\\':
                    if(!escaped) {
                        append = false;
                        escaped = true;
                    }
                    break;

                case ';':
                    if(!inQuote && !inString)
                        break finalLoop;
            }
            if(append)
                builder.append(c);
        }
        return builder.toString();
    }

    private String readArguments(char[] chars, int offset) {
        StringBuilder builder = new StringBuilder();

        boolean inString = false;
        boolean inQuote = false;
        int unclosedBrackets = 1;
        boolean escaped = false;
        finalLoop: for(int i = offset+1;i<chars.length;i++) {
            char c = chars[i];
            boolean append = true;
            switch (c) {
                case '(':
                    unclosedBrackets++;
                    break;

                case ')':
                    unclosedBrackets--;
                    if(unclosedBrackets == 0) {
                        break finalLoop;
                    }
                    break;

                case '"':
                    if(!inQuote && !escaped)
                        inString = !inString;
                    break;

                case '\'':
                    if(!inString && !escaped)
                        inQuote = !inQuote;
                    break;

                case '\\':
                    if(!escaped) {
                        append = false;
                        escaped = true;
                    }
                    break;
            }
            if(append)
                builder.append(c);
        }
        return builder.toString();
    }

    /**
     * Extracts a single method from the source
     * @param chars
     *              The source characters
     * @param i
     *              The offset at which to start the reading
     * @param type
     *              The return returnType
     * @param name
     *              The method name
     * @param access
     *              The method access modifier
     * @return
     *              The extracted method
     */
    private WeacParsedMethod readFunction(char[] chars, int i, Identifier type, Identifier name, WeacModifier access) {
        final int start = i;
        WeacParsedMethod method = new WeacParsedMethod();
        method.returnType = type;
        method.name = name;
        method.access = access;
        System.out.println(">>>! "+name);
        String allArgs = readArguments(chars, i);
        System.out.println(">>> "+allArgs);
        String[] arguments = allArgs.split(",");
        for(String arg : arguments) {
            arg = trimStartingSpace(arg);
            String[] parts = arg.split(" ");
            Identifier argType = new Identifier(parts[0]);
            Identifier argName = new Identifier(parts[1]);
            method.argumentTypes.add(argType);
            method.argumentNames.add(argName);
        }

        StringBuilder methodSource = new StringBuilder();
        i+=allArgs.length();
        int codeStart = i+readUntil(chars, i, '{').length()+1;
        codeStart+=readUntilNot(chars, codeStart, '\n').length();

        int unclosedBrackets = 1;
        boolean inString = false;
        boolean inQuote = false;
        boolean escaped = false;
        int j = codeStart;
        bracketLoop: for(;j<chars.length;j++) {
            char c = chars[j];
            boolean append = true;
            switch (c) {
                case '{':
                    unclosedBrackets++;
                    break;

                case '}':
                    unclosedBrackets--;
                    if(unclosedBrackets == 0) {
                        break bracketLoop;
                    }
                    break;

                case '"':
                    if(!inQuote && !escaped)
                        inString = !inString;
                    if(escaped)
                        escaped = false;
                    break;

                case '\'':
                    if(!inString && !escaped)
                        inQuote = !inQuote;
                    if(escaped)
                        escaped = false;
                    break;

                case '\\':
                    if(!escaped) {
                        append = false;
                        escaped = true;
                    }
                    else
                        escaped = false;
                    break;
            }
            if(append)
                methodSource.append(c);
        }
        method.methodSource = methodSource.toString();
        method.off = (j+1) - start;
        return method;
    }

    /**
     * Parses the header of the class. Currently only find the class returnType and the hierarchy
     * @param parsedClass
     *                  The current class
     * @param header
     *                  The header code
     */
    private void parseHeader(WeacParsedClass parsedClass, String header) {
        String firstPart = readUntilSpace(header);
        switch (firstPart) {
            case "class":
            case "struct":
            case "enum":
            case "interface":
            case "object":
                parsedClass.classType = EnumClassTypes.valueOf(firstPart.toUpperCase());
                readHierarchy(parsedClass, trimStartingSpace(header.replaceFirst(firstPart, "")));
                break;

            default:
                newError("Unknown token "+firstPart, parsedClass.startingLine);
                break;
        }
    }

    /**
     * Parses the hierarchy of the class
     * @param parsedClass
     *              The current class
     * @param s
     *              The hierarchy code
     */
    private void readHierarchy(WeacParsedClass parsedClass, String s) {
        String name = readUntilSpace(s);
        parsedClass.name = name;
        char[] chars = s.toCharArray();
        StringBuilder buffer = new StringBuilder();
        for(int i = name.length();i<s.length();i++) {
            char c = chars[i];
            if(c == '>') {
                i++;
                if(parsedClass.motherClass != null) {
                    newError("A class can only have one mother class", parsedClass.startingLine);
                } else {
                    parsedClass.motherClass = extractClassName(buffer, s, i);
                    buffer.delete(0, buffer.length());
                }
            } else if(c == '+') {
                i++;
                String extracted = extractClassName(buffer, s, i);
                parsedClass.interfacesImplemented.add(extracted);
                buffer.delete(0, buffer.length());
            }
        }

        if(buffer.length() != 0) {
            if(parsedClass.motherClass == null) {
                parsedClass.motherClass = buffer.toString();
            } else {
                parsedClass.interfacesImplemented.add(buffer.toString());
            }
        }
    }

    /**
     * Extracts a single class name
     * @param buffer
     * @param s
     * @param i
     * @return
     */
    private String extractClassName(StringBuilder buffer, String s, int i) {
        char[] chars = s.toCharArray();
        for(int j = i;j<s.length();j++) {
            char c = chars[j];
            if(c == ' ') {
                if(buffer.length() != 0) {
                    return buffer.toString();
                }
            } else {
                if (Character.isJavaIdentifierPart(c)) {
                    buffer.append(c);
                } else {
                    return buffer.toString();
                }
            }
        }
        return buffer.toString();
    }

}
