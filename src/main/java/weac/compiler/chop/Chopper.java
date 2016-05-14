package weac.compiler.chop;

import weac.compiler.CompilePhase;
import weac.compiler.chop.structure.ChoppedSource;
import weac.compiler.chop.structure.ChoppedAnnotation;
import weac.compiler.chop.structure.ChoppedClass;
import weac.compiler.utils.*;

import java.util.*;

/**
 * Chops a WeaC source file
 */
public class Chopper extends CompilePhase<SourceCode, ChoppedSource> {

    /**
     * The class chopping helper
     */
    private final ClassChopper classChopper;

    public Chopper() {
        classChopper = new ClassChopper();
    }

    /**
     * Chops the given source
     * @param source
     *              The source code
     * @return
     *              The parsed source, contains all the extracted data from the source file
     */
    public ChoppedSource process(SourceCode source) {
        ChoppedSource choppedSource = new ChoppedSource();
        choppedSource.imports = new ArrayList<>();
        choppedSource.classes = new ArrayList<>();
        choppedSource.sourceCode = removeComments(source.getContent());
        choppedSource.fileName = source.getFileName();
        analyseHeader(choppedSource, choppedSource.sourceCode);
        return choppedSource;
    }

    @Override
    public Class<SourceCode> getInputClass() {
        return SourceCode.class;
    }

    @Override
    public Class<ChoppedSource> getOutputClass() {
        return ChoppedSource.class;
    }

    /**
     * Chops the source file in order to find the package declaration, the imports and the classes
     * @param choppedSource
     *                  The current source
     * @param source
     *                  The source code
     */
    private void analyseHeader(ChoppedSource choppedSource, String source) {
        source = source.replace("\r", "");
        char[] chars = source.toCharArray();
        int lineIndex = 0;
        List<Modifier> modifiers = new LinkedList<>();
        for(int i = 0;i<chars.length;i++) {
            i += readUntilNot(chars, i, ' ', '\n').length();
            String command = readUntil(chars, i, ' ', '\n');
            switch (command) {
                case "package":
                    i += command.length();
                    i += readUntilNot(chars, i, ' ', '\n').length();
                    String packageName = readUntil(chars, i, ' ', '\n');
                    if(choppedSource.packageName != null) {
                        newError("Cannot set package name twice", lineIndex);
                    } else {
                        choppedSource.packageName = packageName;
                    }
                    i += packageName.length();
                    break;

                case "#target":
                    i += command.length();
                    i += readUntilNot(chars, i, ' ', '\n').length();
                    choppedSource.target = readUntil(chars, i, '\n').replace("\r", "");
                    break;

                case "#version":
                    i += command.length();
                    i += readUntilNot(chars, i, ' ', '\n').length();
                    choppedSource.version = readUntil(chars, i, '\n').replace("\r", "");
                    break;

                case "import":
                    i += command.length();
                    i += readUntilNot(chars, i, ' ', '\n').length();
                    i += readImport(choppedSource, chars, i, lineIndex);
                    break;

                default:
                    i += readModifiers(chars, i, modifiers);
                    ModifierType currentAccess = null;
                    boolean isAbstract = false;
                    boolean isMixin = false;
                    boolean isCompilerSpecial = false;
                    boolean isFinal = false;
                    List<ChoppedAnnotation> annotations = new ArrayList<>();
                    for(Modifier modif : modifiers) {
                        if(modif.getType().isAccessModifier()) {
                            if(currentAccess != null) {
                                newError("Cannot specify twice the access permissions", lineIndex);
                            } else {
                                currentAccess = modif.getType();
                            }
                        } else if(modif.getType() == ModifierType.ABSTRACT) {
                            isAbstract = true;
                        } else if(modif.getType() == ModifierType.MIXIN) {
                            isMixin = true;
                        } else if(modif.getType() == ModifierType.FINAL) {
                            isFinal = true;
                        } else if(modif.getType() == ModifierType.ANNOTATION) {
                            ChoppedAnnotation annot = ((AnnotationModifier) modif).getAnnotation();
                            annotations.add(annot);
                        } else if(modif.getType() == ModifierType.COMPILERSPECIAL) {
                            isCompilerSpecial = true;
                        }
                    }
                    modifiers.clear();
                    if(currentAccess == null)
                        currentAccess = ModifierType.PUBLIC;
                    String extractedClass = extractClass(chars, i);
                    ChoppedClass clazz = readClass(choppedSource, extractedClass, lineIndex, isAbstract, isMixin);
                    clazz.access = currentAccess;
                    clazz.annotations = annotations;
                    clazz.isCompilerSpecial = isCompilerSpecial;
                    clazz.isFinal = isFinal;
                    i+=extractedClass.length();
                    break;
            }
            if(chars[i] == '\n')
                lineIndex++;
        }

        if(choppedSource.target == null)
            choppedSource.target = "jvm";
        if(choppedSource.version == null)
            choppedSource.version = Constants.CURRENT_VERSION;
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
     * Extracts an import from the source and stores it into <code>choppedSource</code>.
     * @param choppedSource
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
    private int readImport(ChoppedSource choppedSource, char[] chars, int offset, int lineIndex) {
        final int start = offset;
        String importStatement = readUntil(chars, offset, '\n');
        String[] parts = importStatement.split(" ");
        offset += importStatement.length();
        Import parsedImport = new Import();
        parsedImport.importedType = parts[0];
        if(parts.length > 2) {
            if(parts[1].equals("as")) {
                parsedImport.usageName = parts[2];
            } else {
                newError("Import statement not understood, should follow: 'import <name>' or 'import <name> as <usageName>''", lineIndex);
            }
        }
        choppedSource.imports.add(parsedImport);
        return offset-start;
    }

    /**
     * Parses a class and stores it to <code>choppedSource</code>.
     * @param choppedSource
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
    private ChoppedClass readClass(ChoppedSource choppedSource, String classSource, int startingLine, boolean isAbstract, boolean isMixin) {
        ChoppedClass choppedClass = classChopper.parseClass(classSource, startingLine);
        choppedClass.packageName = choppedSource.packageName;
        choppedClass.isAbstract = isAbstract;
        choppedClass.isMixin = isMixin;
        choppedSource.classes.add(choppedClass);
        return choppedClass;
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
