package weac.compiler.chop;

import weac.compiler.CompileUtils;
import weac.compiler.chop.structure.ChoppedAnnotation;
import weac.compiler.chop.structure.ChoppedClass;
import weac.compiler.chop.structure.ChoppedField;
import weac.compiler.chop.structure.ChoppedMethod;
import weac.compiler.parser.Parser;
import weac.compiler.utils.AnnotationModifier;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.Modifier;
import weac.compiler.utils.ModifierType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ClassBodyChopper extends CompileUtils {

    /**
     * Parses the body of the class. Locates methods and fields
     * @param choppedClass
     *                  The current class
     * @param body
     *                  The body code
     * @param startingLine
     *                  The line at which the class starts in the source file
     */
    public void parseBody(ChoppedClass choppedClass, String body, int startingLine) {
        Parser parser = new Parser(body);
        parser.enableBlocks().addBlockDelimiters("\"", "\"", false).addBlockDelimiters("{", "}", true);
        parser.forwardUntilNot(" ");

        String emptyBeginning = parser.forwardUntilNotList(" ", "\n");
        startingLine += count(emptyBeginning, '\n');

        if(choppedClass.classType == EnumClassTypes.ENUM) {
            if(parser.hasReachedEnd()) {
                return;
            }
            String constants = parser.forwardTo(";");

            parser.forwardUntilNotList(" ", "\n");

            fillEnumConstants(constants, choppedClass.enumConstants);

            if(parser.hasReachedEnd()) // We might have reached end of file
            {
                return;
            }
        }

        int lineIndex = 0;
        List<Modifier> modifiers = new LinkedList<>();
        while(!parser.hasReachedEnd()) {
            String read = parser.forwardUntilNotList(" ", "\n");
            lineIndex += count(read, '\n');
            if(parser.hasReachedEnd()) {
                break;
            }
            parser.forward(readModifiers(body.toCharArray(), parser.getPosition(), modifiers)); // TODO: change when full migration done
            ModifierType currentAccess = null;
            boolean isAbstract = false;
            boolean isMixin = false;
            boolean isCompilerSpecial = false;

            List<ChoppedAnnotation> annotations = new ArrayList<>();
            for(Modifier modif : modifiers) {
                if(modif.getType().isAccessModifier()) {
                    if(currentAccess != null) {
                        newError("Cannot specify twice the access permissions", lineIndex+startingLine);
                    } else {
                        currentAccess = modif.getType();
                    }
                } else if(modif.getType() == ModifierType.ABSTRACT) {
                    isAbstract = true;
                } else if(modif.getType() == ModifierType.MIXIN) {
                    isMixin = true;
                } else if(modif.getType() == ModifierType.ANNOTATION) {
                    annotations.add(((AnnotationModifier) modif).getAnnotation());
                } else if(modif.getType() == ModifierType.COMPILERSPECIAL) {
                    isCompilerSpecial = true;
                }
            }
            modifiers.clear();
            if(currentAccess == null)
                currentAccess = ModifierType.PUBLIC;
            if(choppedClass.classType == EnumClassTypes.INTERFACE)
                isAbstract = true; // interfaces methods are always abstract
            else if(choppedClass.isMixin)
                isAbstract = true; // mixin (are analog to interfaces for the JVM) methods are always abstract
            else if(choppedClass.classType == EnumClassTypes.ANNOTATION)
                isAbstract = true; // annotation methods are always abstract
            Identifier firstPart = Identifier.read(body.toCharArray(), parser.getPosition());
            if(firstPart.isValid()) {
                parser.forward(firstPart.getId().length()); // TODO: remove when full migration done
                parser.forwardUntilNotList(" ", "\n");
                if(parser.isAt("(")) { // Constructor

                    ChoppedMethod function = readFunction(body.toCharArray(), parser.getPosition(), choppedClass, Identifier.VOID, firstPart, currentAccess, isAbstract);
                    function.isCompilerSpecial = isCompilerSpecial;
                    function.annotations = annotations;
                    function.startingLine = lineIndex+startingLine;
                    function.isConstructor = true;
                    function.name = new Identifier("<init>");
                    choppedClass.methods.add(function);
                    parser.forward(function.off); // TODO: remove when full migration done

                } else {
                    Identifier secondPart = Identifier.read(body.toCharArray(), parser.getPosition());
                    parser.forward(secondPart.getId().length()); // TODO: remove when full migration done
                    if(!secondPart.isValid()) {
                        newError("Invalid identifier: "+secondPart.getId()+" in "+ choppedClass.packageName+"."+ choppedClass.name, startingLine);
                    } else {
                        String closest = parser.getClosest("(", ";", "=");

                        String start = null;
                        boolean isField = false;
                        parser.mark();
                        if(closest.equals("=") || closest.equals(";")) {
                            isField = true;
                            start = parser.forwardTo(";");
                            parser.forward(1);
                        } else if(closest.equals("(")) {
                            isField = false;
                            start = parser.forwardTo("(");
                            parser.rewind();
                        }
                        if(isField) {
                            ChoppedField field = new ChoppedField();
                            field.isCompilerSpecial = isCompilerSpecial;
                            field.annotations = annotations;
                            field.name = secondPart;
                            field.type = firstPart;
                            field.access = currentAccess;
                            field.startingLine = startingLine+lineIndex;
                            if(start.contains("=")) {
                                field.defaultValue = trimStartingSpace(start.split("=")[1]);
                            }
                            choppedClass.fields.add(field);
                            parser.discardMark();
                        } else {
                            ChoppedMethod function = readFunction(body.toCharArray(), parser.getPosition(), choppedClass, firstPart, secondPart, currentAccess, isAbstract);
                            function.isCompilerSpecial = isCompilerSpecial;
                            function.annotations = annotations;
                            function.startingLine = lineIndex+startingLine;
                            choppedClass.methods.add(function);
                            parser.forward(function.off); // TODO: remove when full migration done
                        }
                    }
                }
            } else {
                newError("Invalid identifier: " + firstPart.getId()+" in "+ choppedClass.packageName+"."+ choppedClass.name+" from "+body.substring(parser.getPosition(), body.length()), startingLine+lineIndex);
            }

            if(parser.isAt("\n"))
                lineIndex++;

            parser.forward(1);
        }
    }

    private int count(String s, char c) {
        int count = 0;
        for (char ch : s.toCharArray())
            if(ch == c)
                count++;
        return count;
    }

    private void fillEnumConstants(String constantList, List<String> out) {
        int i = 0; // TODO: replace with new parser
        String constant = readSingleArgument(constantList, 0, true).replace("\n", "");
        while(!constant.isEmpty()) {
            out.add(constant);

            i += constant.length()+1;

            i += readUntilNot(constantList.toCharArray(), i, ' ', '\n', ',', '\r').length();
            constant = readSingleArgument(constantList, i, true).replace("\n", "");
        }
    }

    /**
     * Extracts a single method from the source
     * @param chars
     *              The source characters
     * @param i
     *              The offset at which to start the reading
     * @param choppedClass
     *              The owner of this method
     * @param type
     *              The return returnType
     * @param name
     *                  The method name
     * @param access
     *                   The method access modifier
     * @param isAbstract
     *              True if the method abstract and has no implementation
     * @return
     *              The extracted method
     */
    private ChoppedMethod readFunction(char[] chars, int i, ChoppedClass choppedClass, Identifier type, Identifier name, ModifierType access, boolean isAbstract) {
        final int start = i;
        ChoppedMethod method = new ChoppedMethod();
        method.returnType = type;
        method.name = name;
        method.isAbstract = isAbstract;
        method.access = access;
        String allArgs = readArguments(chars, i);
        String[] arguments = allArgs.split(",");
        for(String arg : arguments) {
            arg = trimStartingSpace(arg);
            if(arg.isEmpty()) {
                continue;
            }
            String[] parts = arg.split(" ");
            Identifier argType = new Identifier(parts[0]);
            Identifier argName = new Identifier(parts[1]);
            method.argumentTypes.add(argType);
            method.argumentNames.add(argName);
        }

        i+=allArgs.length();
        if(isAbstract || choppedClass.classType == EnumClassTypes.INTERFACE || choppedClass.classType == EnumClassTypes.ANNOTATION) {
            i+=1;
            method.methodSource = "";
            method.isAbstract = true;
            method.off = i - start;
        } else {
            int codeStart = i + readUntil(chars, i, '{').length()+1;
            codeStart += readUntilNot(chars, codeStart, '\n').length();
            method.methodSource = readCodeblock(chars, codeStart);
            method.off = (method.methodSource.length()+1+codeStart)-start;
        }
        return method;
    }

}
