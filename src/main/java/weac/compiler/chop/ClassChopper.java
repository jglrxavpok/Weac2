package weac.compiler.chop;

import weac.compiler.CompileUtils;
import weac.compiler.chop.structure.ChoppedClass;
import weac.compiler.targets.jvm.JVMWeacTypes;
import weac.compiler.utils.WeacType;

import java.util.ArrayList;

/**
 * Parses a class source to find its components: hierarchy, fields, and methods.
 */
public class ClassChopper extends CompileUtils {

    private final ClassBodyChopper bodyParser;

    public ClassChopper() {
        bodyParser = new ClassBodyChopper();
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
    public ChoppedClass parseClass(String source, int startingLine) {
        ChoppedClass choppedClass = new ChoppedClass();
        choppedClass.startingLine = startingLine;
        choppedClass.enumConstants = new ArrayList<>();
        choppedClass.fields = new ArrayList<>();
        choppedClass.methods = new ArrayList<>();
        choppedClass.interfacesImplemented = new ArrayList<>();
        String header = source.substring(0, source.indexOf('{'));
        parseHeader(choppedClass, header);
        String body = source.substring(source.indexOf('{')+1, source.length()-1);
        bodyParser.parseBody(choppedClass, body, startingLine);
        return choppedClass;
    }

    /**
     * Parses the header of the class. Currently only find the class returnType and the hierarchy
     * @param choppedClass
     *                  The current class
     * @param header
     *                  The header code
     */
    private void parseHeader(ChoppedClass choppedClass, String header) {
        char[] chars = header.toCharArray();
        int start = readUntilNot(chars, 0, ' ', '\t', '\n').length();
        String firstPart = readUntil(chars, start, ' ');
        switch (firstPart) {
            case "class":
            case "data":
            case "enum":
            case "interface":
            case "object":
            case "annotation":
                choppedClass.classType = EnumClassTypes.valueOf(firstPart.toUpperCase());
                readHierarchy(choppedClass, trimStartingSpace(header.replaceFirst(firstPart, "")));
                break;

            default:
                // assume it's a class
                choppedClass.classType = EnumClassTypes.CLASS;
                readHierarchy(choppedClass, trimStartingSpace(header));
                break;
        }
    }

    /**
     * Parses the hierarchy of the class
     * @param choppedClass
     *              The current class
     * @param s
     *              The hierarchy code
     */
    private void readHierarchy(ChoppedClass choppedClass, String s) {
        char[] chars = s.toCharArray();
        int start = readUntilNot(chars, 0, ' ', '\n').length();
        String name = readUntil(chars, start, ' ', '\n');
        choppedClass.name = new WeacType(JVMWeacTypes.OBJECT_TYPE, name, false);
        if(name.isEmpty())
            System.out.println("!!!"+s.substring(start));
        StringBuilder buffer = new StringBuilder();
        for(int i = name.length()+start;i<chars.length;i++) {
            char c = chars[i];
            if(c == '>') {
                i++;
                if(choppedClass.motherClass != null) {
                    newError("A class can only have one mother class", choppedClass.startingLine);
                } else {
                    choppedClass.motherClass = extractClassName(buffer, s, i);
                    buffer.delete(0, buffer.length());
                }
            } else if(c == '+') {
                i++;
                String extracted = extractClassName(buffer, s, i);
                choppedClass.interfacesImplemented.add(extracted);
                buffer.delete(0, buffer.length());
            }
        }

        if(buffer.length() != 0) {
            if(choppedClass.motherClass == null) {
                choppedClass.motherClass = buffer.toString();
            } else {
                choppedClass.interfacesImplemented.add(buffer.toString());
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
