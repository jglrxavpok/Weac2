package weac.compiler.parse;

import weac.compiler.CompileUtils;
import weac.compiler.parse.structure.ParsedClass;
import weac.compiler.utils.WeacType;

import java.util.ArrayList;

/**
 * Parses a class source to find its components: hierarchy, fields, and methods.
 */
public class ClassParser extends CompileUtils {

    private final ClassBodyParser bodyParser;

    public ClassParser() {
        bodyParser = new ClassBodyParser();
    }

    /**-
     * Parses a single class from the given source
     * @param source
     *              The class code
     * @param startingLine
     *              The line at which the class starts in the source file
     * @return
     *              The class extracted from the source file
     */
    public ParsedClass parseClass(String source, int startingLine) {
        ParsedClass parsedClass = new ParsedClass();
        parsedClass.startingLine = startingLine;
        parsedClass.enumConstants = new ArrayList<>();
        parsedClass.fields = new ArrayList<>();
        parsedClass.methods = new ArrayList<>();
        parsedClass.interfacesImplemented = new ArrayList<>();
        String header = source.substring(0, source.indexOf('{'));
        parseHeader(parsedClass, header);
        String body = source.substring(source.indexOf('{')+1, source.length()-1);
        bodyParser.parseBody(parsedClass, body, startingLine);
        return parsedClass;
    }

    /**
     * Parses the header of the class. Currently only find the class returnType and the hierarchy
     * @param parsedClass
     *                  The current class
     * @param header
     *                  The header code
     */
    private void parseHeader(ParsedClass parsedClass, String header) {
        char[] chars = header.toCharArray();
        int start = readUntilNot(chars, 0, ' ', '\t', '\n').length();
        String firstPart = readUntil(chars, start, ' ');
        switch (firstPart) {
            case "class":
            case "struct":
            case "enum":
            case "interface":
            case "object":
            case "annotation":
                parsedClass.classType = EnumClassTypes.valueOf(firstPart.toUpperCase());
                readHierarchy(parsedClass, trimStartingSpace(header.replaceFirst(firstPart, "")));
                break;

            default:
                // assume it's a class
                parsedClass.classType = EnumClassTypes.CLASS;
                readHierarchy(parsedClass, trimStartingSpace(header));
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
    private void readHierarchy(ParsedClass parsedClass, String s) {
        char[] chars = s.toCharArray();
        int start = readUntilNot(chars, 0, ' ', '\n').length();
        String name = readUntil(chars, start, ' ', '\n');
        parsedClass.name = new WeacType(WeacType.OBJECT_TYPE, name, false);
        if(name.isEmpty())
            System.out.println("!!!"+s.substring(start));
        StringBuilder buffer = new StringBuilder();
        for(int i = name.length()+start;i<chars.length;i++) {
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
