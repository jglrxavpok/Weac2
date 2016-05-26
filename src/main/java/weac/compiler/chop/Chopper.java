package weac.compiler.chop;

import weac.compiler.CompilePhase;
import weac.compiler.chop.structure.ChoppedSource;
import weac.compiler.chop.structure.ChoppedAnnotation;
import weac.compiler.chop.structure.ChoppedClass;
import weac.compiler.parser.ParseRule;
import weac.compiler.parser.Parser;
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
    private final Parser importParser;
    private final ParseRule commentRule;

    public Chopper() {
        classChopper = new ClassChopper();
        commentRule = new ParseRule("/");
        commentRule.setAction(p -> {
            char next = p.nextCharacter();
            if(next == '/') {
                p.forwardTo("\n");
                //lineIndex++;
            } else if(next == '*') {
                while(p.nextCharacter() != '/') {
                    p.backwards(1);
                    String read = p.forwardTo("*");
                    /*lineIndex += read.chars()
                            .boxed()
                            .filter(c -> c == '\n')
                            .count();*/ // TODO: line number
                }
                p.backwards(1);
            }
        });
        importParser = new Parser();
        importParser.enableBlocks().addBlockDelimiters("{", "}", false);
        importParser.newRule("import", importRule -> importRule.setAction((p) -> {
            importParser.forwardUntilNotList(" ", "\n");
            String end = importParser.getClosest(" ", "\n");
            String imported = end == null ? importParser.forwardToEnd() : importParser.forwardToOrEnd(end);
            importParser.forwardUntilNot(" ");
            String usageName = null;
            if(importParser.isAt("as ")) {
                importParser.forward(3);
                importParser.forwardUntilNotList(" ", "\n");
                if(importParser.isAt("\n") || importParser.hasReachedEnd()) {
                    newError("Invalid import, must have an usage name after 'as'", -1); // todo line
                } else {
                    usageName = importParser.forwardToOrEndList(" ", "\n");
                }
            }
            Import importResult = new Import();
            importResult.importedType = imported;
            importResult.usageName = usageName;
            importParser.setUserObject(importResult);
        }));
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
        choppedSource.sourceCode = source.getContent();
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
        Parser parser = new Parser(source);
        parser.enableBlocks();
        parser.addBlockDelimiters("{", "}", true);
        parser.addBlockDelimiters("\"", "\"", false);
        parser.addBlockDelimiters("'", "'", false);
        while(!parser.hasReachedEnd()) {
            parser.forwardUntilNotList(" ", "\n");
            parser.applyRuleIfPossible(commentRule);
            parser.mark();
            String command = parser.forwardToList(" ", "\n");
            if(command == null)
                command = parser.forwardToEnd();
            switch (command) {
                case "package":
                    parser.discardMark();
                    parser.forwardUntilNotList(" ", "\n");
                    String packageName = parser.forwardToList(" ", "\n");
                    if(choppedSource.packageName != null) {
                        newError("Cannot set package name twice, was "+choppedSource.packageName, lineIndex);
                    } else {
                        choppedSource.packageName = packageName;
                    }
                    break;

                case "#target":
                    parser.discardMark();
                    parser.forwardUntilNotList(" ", "\n");
                    choppedSource.target = parser.forwardToOrEnd("\n").replace("\r", "");
                    break;

                case "#version":
                    parser.discardMark();
                    parser.forwardUntilNotList(" ", "\n");
                    choppedSource.version = parser.forwardToOrEnd("\n").replace("\r", "");
                    break;

                case "import":
                    parser.rewind();
                    readImport(choppedSource, parser, lineIndex);
                    break;

                default:
                    if(parser.hasReachedEnd())
                        break;
                    parser.rewind();
                    modifiers = readModifiers(parser);
                    parser.forwardUntilNotList(" ", "\n", "\r");
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
                    try {
                        String extractedClass = extractClass(parser);
                        ChoppedClass clazz = readClass(choppedSource, extractedClass, lineIndex, isAbstract, isMixin);
                        clazz.access = currentAccess;
                        clazz.annotations = annotations;
                        clazz.isCompilerSpecial = isCompilerSpecial;
                        clazz.isFinal = isFinal;
                    }
                    catch (Exception e) {
                        System.err.println(parser.getData().substring(parser.getPosition()));
                        e.printStackTrace();
                    }
                    break;
            }
            if(parser.isAt("\n"))
                lineIndex++;
        }

        if(choppedSource.target == null)
            choppedSource.target = "jvm";
        if(choppedSource.version == null)
            choppedSource.version = Constants.CURRENT_VERSION;

        choppedSource.classes.forEach(clazz -> {
            if(!clazz.getCanonicalName().equals(Constants.WEAC_VERSION_ANNOTATION)) {
                ChoppedAnnotation versionAnnotation = new ChoppedAnnotation("WeacVersion");
                versionAnnotation.args.add("\""+choppedSource.version+"\"");
                clazz.annotations.add(versionAnnotation);
            }
        });
    }

    /**
     * Extracts a single class from the source code
     * @return
     *                  The extracted class source code
     */
    private String extractClass(Parser parser) {
        return parser.forwardTo("}")+parser.forward(1);
    }

    /**
     * Extracts an import from the source and stores it into <code>choppedSource</code>.
     * @param choppedSource
     *                  The current source
     * @param lineIndex
     *                  The line of the import
     * @return
     *                  The number of characters read
     */
    private void readImport(ChoppedSource choppedSource, Parser parser, int lineIndex) {
        String importStatement = parser.forwardToOrEnd("\n");
        importParser.setData(importStatement);
        importParser.applyRules();
        Import importObj = (Import) importParser.getUserObject();
        choppedSource.imports.add(importObj);
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

}
