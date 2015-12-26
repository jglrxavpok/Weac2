package org.jglr.weac.parse;

import org.jglr.weac.WeacCompilePhase;
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
public class WeacClassParser extends WeacCompilePhase {

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
            if(i == chars.length)
                break;
            Identifier firstPart = Identifier.read(chars, i);
            if(firstPart.isValid()) {
                i += firstPart.getId().length()+1;
                i += readUntilNot(chars, i, ' ', '\n').length();
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
                        i = function.methodSource.length()+1+nameEnd;
                    }
                }
            } else {
                newError("Invalid identifier: " + firstPart.getId(), startingLine+lineIndex);
            }
        }
    }

    /**
     * Extracts a single method from the source
     * @param chars
     *              The source characters
     * @param i
     *              The offset at which to start the reading
     * @param type
     *              The return type
     * @param name
     *              The method name
     * @param access
     *              The method access modifier
     * @return
     *              The extracted method
     */
    private WeacParsedMethod readFunction(char[] chars, int i, Identifier type, Identifier name, WeacModifier access) {
        WeacParsedMethod method = new WeacParsedMethod();
        method.type = type;
        method.name = name;
        method.access = access;
        int argStart = indexOf(chars, i, '(')+1;
        String allArgs = readUntil(chars, argStart, ')');
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
        int codeStart = argStart+allArgs.length()+readUntil(chars, argStart+allArgs.length(), '{').length()+1;
        int unclosedBrackets = 1;
        for(int j = codeStart;j<chars.length;j++) {
            if(chars[j] == '{') {
                unclosedBrackets++;
            } else if(chars[j] == '}') {
                unclosedBrackets--;
                if(unclosedBrackets == 0) {
                    break;
                }
            }
            methodSource.append(chars[j]);
        }
        method.methodSource = methodSource.toString();
        return method;
    }

    /**
     * Parses the header of the class. Currently only find the class type and the hierarchy
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
