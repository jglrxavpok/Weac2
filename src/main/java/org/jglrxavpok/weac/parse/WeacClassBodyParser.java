package org.jglrxavpok.weac.parse;

import org.jglrxavpok.weac.WeacCompileUtils;
import org.jglrxavpok.weac.parse.structure.WeacParsedAnnotation;
import org.jglrxavpok.weac.parse.structure.WeacParsedClass;
import org.jglrxavpok.weac.parse.structure.WeacParsedField;
import org.jglrxavpok.weac.parse.structure.WeacParsedMethod;
import org.jglrxavpok.weac.utils.AnnotationModifier;
import org.jglrxavpok.weac.utils.Identifier;
import org.jglrxavpok.weac.utils.WeacModifier;
import org.jglrxavpok.weac.utils.WeacModifierType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WeacClassBodyParser extends WeacCompileUtils {

    /**
     * Parses the body of the class. Locates methods and fields
     * @param parsedClass
     *                  The current class
     * @param body
     *                  The body code
     * @param startingLine
     *                  The line at which the class starts in the source file
     */
    public void parseBody(WeacParsedClass parsedClass, String body, int startingLine) {
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
            WeacModifierType currentAccess = null;
            boolean isAbstract = false;
            boolean isMixin = false;
            boolean isCompilerSpecial = false;

            List<WeacParsedAnnotation> annotations = new ArrayList<>();
            for(WeacModifier modif : modifiers) {
                if(modif.getType().isAccessModifier()) {
                    if(currentAccess != null) {
                        newError("Cannot specify twice the access permissions", lineIndex+startingLine);
                    } else {
                        currentAccess = modif.getType();
                    }
                } else if(modif.getType() == WeacModifierType.ABSTRACT) {
                    isAbstract = true;
                } else if(modif.getType() == WeacModifierType.MIXIN) {
                    isMixin = true;
                } else if(modif.getType() == WeacModifierType.ANNOTATION) {
                    annotations.add(((AnnotationModifier) modif).getAnnotation());
                } else if(modif.getType() == WeacModifierType.COMPILERSPECIAL) {
                    isCompilerSpecial = true;
                }
            }
            modifiers.clear();
            if(currentAccess == null)
                currentAccess = WeacModifierType.PUBLIC;
            i += readUntilNot(chars, i, ' ', '\n').length();
            if(i >= chars.length)
                break;
            if(parsedClass.classType == EnumClassTypes.ENUM) {
                String constants = readUntilInsnEnd(chars, i);

                i += constants.length()+1;
                i += readUntilNot(chars, i, ' ', '\n').length();

                fillEnumConstants(constants, parsedClass.enumConstants);

                if(i >= chars.length) // We might have reached end of file
                    break;
            }
            Identifier firstPart = Identifier.read(chars, i);
            if(firstPart.isValid()) {
                i += firstPart.getId().length();
                i += readUntilNot(chars, i, ' ', '\n').length();
                if(chars[i] == '(') { // Constructor

                    WeacParsedMethod function = readFunction(chars, i, parsedClass, Identifier.VOID, firstPart, currentAccess, isAbstract);
                    function.isCompilerSpecial = isCompilerSpecial;
                    function.annotations = annotations;
                    function.startingLine = lineIndex+startingLine;
                    function.isConstructor = true;
                    parsedClass.methods.add(function);
                    i += function.off;

                } else {
                    Identifier secondPart = Identifier.read(chars, i);
                    i+=secondPart.getId().length();
                    if(!secondPart.isValid()) {
                        newError("Invalid identifier: "+secondPart.getId()+" in "+parsedClass.packageName+"."+parsedClass.name, startingLine);
                    } else {
                        int potentialFunction = indexOf(chars, i, '(');
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
                            field.isCompilerSpecial = isCompilerSpecial;
                            field.annotations = annotations;
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
                            WeacParsedMethod function = readFunction(chars, i, parsedClass, firstPart, secondPart, currentAccess, isAbstract);
                            function.isCompilerSpecial = isCompilerSpecial;
                            function.annotations = annotations;
                            function.startingLine = lineIndex+startingLine;
                            parsedClass.methods.add(function);
                            i += function.off;
                        }
                    }
                }
            } else {
                newError("Invalid identifier: " + firstPart.getId()+" in "+parsedClass.packageName+"."+parsedClass.name+" from "+new String(chars, i, chars.length-i), startingLine+lineIndex);
            }
        }
    }

    private void fillEnumConstants(String constantList, List<String> out) {
        int i = 0;
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
     * @param parsedClass
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
    private WeacParsedMethod readFunction(char[] chars, int i, WeacParsedClass parsedClass, Identifier type, Identifier name, WeacModifierType access, boolean isAbstract) {
        final int start = i;
        WeacParsedMethod method = new WeacParsedMethod();
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
        if(isAbstract || parsedClass.classType == EnumClassTypes.INTERFACE || parsedClass.classType == EnumClassTypes.ANNOTATION) {
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
