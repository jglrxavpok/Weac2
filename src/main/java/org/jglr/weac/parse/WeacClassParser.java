package org.jglr.weac.parse;

import org.jglr.weac.WeacCompilePhase;
import org.jglr.weac.utils.Identifier;

import java.util.ArrayList;

public class WeacClassParser extends WeacCompilePhase {

    public WeacClassParser() {

    }

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

    private void parseBody(WeacParsedClass parsedClass, String body, int startingLine) {
        body = trimStartingSpace(body);
        char[] chars = body.toCharArray();
        int lineIndex = 0;
        for(int i = 0;i<chars.length;i++) {
            char c = chars[i];
            if(c == '\n') {
                lineIndex++;
            }
            String s = readUntil(chars, i, ' ');
            String access = "public";
            boolean isAbstract = false; // TODO
            switch (s) {
                case "public":
                case "private":
                case "protected":
                    access = s;
                    i+=s.length();
                    i+=readUntilNot(chars,i,' ').length();
                    break;

              /*  case "abstract":
                    isAbstract = true;
                    i+=s.length();
                    i+=readUntilNot(chars,i,' ').length();
                    break;*/
            }
            Identifier firstPart = Identifier.read(chars, i);
            if(firstPart.isValid()) {
                i += firstPart.getId().length()+1;
                i += readUntilNot(chars, i, ' ').length();
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
                        field.access = access;
                        field.startingLine = startingLine+lineIndex;
                        if(start.contains("=")) {
                            field.defaultValue = trimStartingSpace(start.split("=")[1]);
                        }
                        parsedClass.fields.add(field);
                        i = nameEnd+1;
                    } else {
                        WeacParsedMethod function = readFunction(chars, i, firstPart, secondPart, access);
                        function.startingLine = lineIndex+startingLine;
                        parsedClass.methods.add(function);
                        i = function.methodSource.length()+1+nameEnd;
                    }
                }
            } else {
                // TODO Handle what it can possibly be.
            }
        }
    }

    private WeacParsedMethod readFunction(char[] chars, int i, Identifier type, Identifier name, String access) {
        WeacParsedMethod method = new WeacParsedMethod();
        method.type = type;
        method.name = name;
        method.access = access;
        String[] arguments = readUntil(chars, indexOf(chars, i, '(')+1, ')').split(",");
        for(String arg : arguments) {
            arg = trimStartingSpace(arg);
            String[] parts = arg.split(" ");
            Identifier argType = new Identifier(parts[0]);
            Identifier argName = new Identifier(parts[1]);
            method.argumentTypes.add(argType);
            method.argumentNames.add(argName);
        }

        StringBuilder methodSource = new StringBuilder();
        int unclosedBrackets = 0;
        for(int j = 0;j<chars.length;j++) {
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

    private String read(char[] array, int start, int end) {
        StringBuilder builder = new StringBuilder();
        for(int i = start;i<end;i++) {
            builder.append(array[i]);
        }
        return builder.toString();
    }

    private int indexOf(char[] array, int start, char toFind) {
        for(int i = start;i<array.length;i++) {
            if(array[i] == toFind)
                return i;
        }
        return -1;
    }

    private String readUntilNot(char[] array, int start, char seeked) {
        StringBuilder builder = new StringBuilder();
        for(int i = start;i<array.length;i++) {
            if(array[i] != seeked)
                break;
            else
                builder.append(array[i]);
        }
        return builder.toString();
    }

    private String readUntil(char[] array, int start, char seeked) {
        StringBuilder builder = new StringBuilder();
        for(int i = start;i<array.length;i++) {
            if(array[i] == seeked)
                break;
            else
                builder.append(array[i]);
        }
        return builder.toString();
    }

    private void parseHeader(WeacParsedClass parsedClass, String header) {
        String firstPart = readUntilSpace(header);
        switch (firstPart) {
            case "public":
            case "protected":
            case "private":
                parsedClass.access = firstPart;
                parseHeader(parsedClass, trimStartingSpace(header.replaceFirst(firstPart, "")));
                break;

            case "class":
            case "struct":
            case "enum":
            case "interface":
                parsedClass.classType = EnumClassTypes.valueOf(firstPart.toUpperCase());
                readHierarchy(parsedClass, trimStartingSpace(header.replaceFirst(firstPart, "")));
                break;

            default:
                newError("Unknown token "+firstPart, parsedClass.startingLine);
                break;
        }
    }

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
