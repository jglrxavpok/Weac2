package org.jglr.weac.parse;

import org.jglr.weac.WeacCompilePhase;
import org.jglr.weac.parse.structure.WeacParsedClass;
import org.jglr.weac.parse.structure.WeacParsedImport;
import org.jglr.weac.parse.structure.WeacParsedSource;
import org.jglr.weac.utils.WeacModifier;
import org.jglr.weac.utils.WeacModifierType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Parses a WeaC source file
 */
public class WeacParser extends WeacCompilePhase<String, WeacParsedSource> {

    /**
     * The class parsing helper
     */
    private final WeacClassParser classParser;

    public WeacParser() {
        classParser = new WeacClassParser();
    }

    /**
     * Parses the given source
     * @param source
     *              The source code
     * @return
     *              The parsed source, contains all the extracted data from the source file
     */
    public WeacParsedSource process(String source) {
        WeacParsedSource parsedSource = new WeacParsedSource();
        parsedSource.imports = new ArrayList<>();
        parsedSource.classes = new ArrayList<>();
        source = removeComments(source);
        analyseHeader(parsedSource, source);
        parsedSource.sourceCode = source;
        return parsedSource;
    }

    @Override
    public Class<String> getInputClass() {
        return String.class;
    }

    @Override
    public Class<WeacParsedSource> getOutputClass() {
        return WeacParsedSource.class;
    }

    /**
     * Parses the source file in order to find the package declaration, the imports and the classes
     * @param parsedSource
     *                  The current source
     * @param source
     *                  The source code
     */
    private void analyseHeader(WeacParsedSource parsedSource, String source) {
        source = source.replace("\r", "");
        char[] chars = source.toCharArray();
        int lineIndex = 0;
        List<WeacModifier> modifiers = new LinkedList<>();
        for(int i = 0;i<chars.length;i++) {
            i += readUntilNot(chars, i, ' ', '\n').length();
            String command = readUntil(chars, i, ' ', '\n');
            switch (command) {
                case "package":
                    i += command.length();
                    i += readUntilNot(chars, i, ' ', '\n').length();
                    String packageName = readUntil(chars, i, ' ', '\n');
                    if(parsedSource.packageName != null) {
                        newError("Cannot set package name twice", lineIndex);
                    } else {
                        parsedSource.packageName = packageName;
                    }
                    i += packageName.length();
                    break;

                case "import":
                    i += command.length();
                    i += readUntilNot(chars, i, ' ', '\n').length();
                    i += readImport(parsedSource, chars, i, lineIndex);
                    break;

                default:
                    i += readModifiers(chars, i, modifiers);
                    WeacModifierType currentAccess = null;
                    boolean isAbstract = false;
                    boolean isMixin = false;
                    for(WeacModifier modif : modifiers) {
                        // TODO: Abstract & mixins
                        if(modif.getType().isAccessModifier()) {
                            if(currentAccess != null) {
                                newError("Cannot specify twice the access permissions", lineIndex);
                            } else {
                                currentAccess = modif.getType();
                            }
                        } else if(modif.getType() == WeacModifierType.ABSTRACT) {
                            isAbstract = true;
                        } else if(modif.getType() == WeacModifierType.MIXIN) {
                            isMixin = true;
                        }
                    }
                    modifiers.clear();
                    if(currentAccess == null)
                        currentAccess = WeacModifierType.PUBLIC;
                    String extractedClass = extractClass(chars, i);
                    WeacParsedClass clazz = readClass(parsedSource, extractedClass, lineIndex, isAbstract, isMixin);
                    clazz.access = currentAccess;
                    i+=extractedClass.length();
                    break;
            }
            if(chars[i] == '\n')
                lineIndex++;
        }
    }

    /**
     * Extracts a single class from the source code
     * @param chars
     *                  The source code characters
     * @param startIndex
     *                  The offset at which to start the extraction
     * @return
     *                  The extracted class source code
     */
    private String extractClass(char[] chars, int startIndex) {
        int unclosedCurlyBrackets = 0;
        StringBuilder builder = new StringBuilder();
        for(int i = startIndex;i<chars.length;i++) {
            char c = chars[i];
            builder.append(c);

            if(c == '{') {
                unclosedCurlyBrackets++;
            } else if(c == '}') {
                unclosedCurlyBrackets--;
                if(unclosedCurlyBrackets == 0) {
                    break;
                }
            }
        }
        return builder.toString();
    }

    /**
     * Extracts an import from the source and stores it into <code>parsedSource</code>.
     * @param parsedSource
     *                  The current source
     * @param chars
     *                  The source characters
     * @param offset
     *                  The offset at which to start extracting the import declaration
     * @param lineIndex
     *                  The line of the import
     * @return
     *                  The number of characters read
     */
    private int readImport(WeacParsedSource parsedSource, char[] chars, int offset, int lineIndex) {
        final int start = offset;
        String importStatement = readUntil(chars, offset, '\n');
        String[] parts = importStatement.split(" ");
        offset += importStatement.length();
        WeacParsedImport parsedImport = new WeacParsedImport();
        parsedImport.importedType = parts[0];
        if(parts.length > 2) {
            if(parts[1].equals("as")) {
                parsedImport.usageName = parts[2];
            } else {
                newError("Import statement not undertstood, should follow: 'import <name>' or 'import <name> as <usageName>''", lineIndex);
            }
        }
        parsedSource.imports.add(parsedImport);
        return offset-start;
    }

    /**
     * Parses a class and stores it to <code>parsedSource</code>.
     * @param parsedSource
     *                  The current source
     * @param classSource
     *                  The source code of the class
     * @param startingLine
     *                  The line at which the class starts inside the source file
     * @param isAbstract
     *                  Is the class abstract?
     * @param isMixin
     *                  Is the class a mixin class?
     * @return
     *                  The parsed class
     */
    private WeacParsedClass readClass(WeacParsedSource parsedSource, String classSource, int startingLine, boolean isAbstract, boolean isMixin) {
        WeacParsedClass parsedClass = classParser.parseClass(classSource, startingLine);
        parsedClass.isAbstract = isAbstract;
        parsedClass.isMixin = isMixin;
        parsedSource.classes.add(parsedClass);
        return parsedClass;
    }

    /**
     * Removes all the comments from the source
     * @param source
     *              The source code
     * @return
     *              The source code without the comments
     */
    private String removeComments(String source) {
        char[] chars = source.toCharArray();
        StringBuilder builder = new StringBuilder();
        boolean inString = false;
        boolean inQuote = false;
        for(int i = 0;i<chars.length;i++) {
            boolean shouldAppend = true;
            char c = chars[i];
            char next = (i < chars.length-1) ? chars[i+1] : '\0';
            if(c == '/' && !inString) {
                if(next == '/') {
                    shouldAppend = false;
                    while(chars[i] != '\n') {
                        i++;
                    }
                } else if(next == '*') {
                    shouldAppend = false;
                    while(i < chars.length-1 && (chars[i] != '/' || chars[i-1] != '*')) {
                        i++;
                    }
                }
            }

            if(c == '\'' && !inString) {
                if(i != 0 && chars[i-1] != '\\') {
                    inQuote = !inQuote;
                }
            }

            if(c == '"' && !inQuote) {
                if(i != 0 && chars[i-1] != '\\') {
                    inString = !inString;
                }
            }

            if(shouldAppend)
                builder.append(c);
        }
        return builder.toString();
    }

}
