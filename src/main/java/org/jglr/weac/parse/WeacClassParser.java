package org.jglr.weac.parse;

import org.jglr.weac.WeacCompilePhase;

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
        return parsedClass;
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
