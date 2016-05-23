package weac.compiler.chop;

import weac.compiler.CompileUtils;
import weac.compiler.chop.structure.ChoppedClass;
import weac.compiler.parser.Parser;
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
        Parser parser = new Parser(s);
        parser.forwardUntilNotList(" ", "\n");
        String name = parser.forwardToList(" ", "\n");
        choppedClass.name = new WeacType(JVMWeacTypes.OBJECT_TYPE, name, false);
        while(!parser.hasReachedEnd()) {
            if(parser.isAt(">")) { // TODO: Change to avoid conflicts in generics
                if(choppedClass.motherClass != null) {
                    newError("A class can only have one mother class", choppedClass.startingLine);
                } else {
                    parser.forward(1); // skip the character
                    parser.forwardUntilNot(" ");
                    choppedClass.motherClass = parser.forwardToOrEnd(" ");
                }
            } else if(parser.isAt("+")) {
                parser.forward(1); // skip the character
                parser.forwardUntilNot(" ");
                String extracted = parser.forwardToOrEnd(" ");
                choppedClass.interfacesImplemented.add(extracted);
            }
            parser.forward(1);
        }
    }

}
