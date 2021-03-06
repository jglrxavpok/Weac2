package weac.compiler;

import weac.compiler.chop.structure.ChoppedAnnotation;
import weac.compiler.parser.Parser;
import weac.compiler.utils.*;

import java.util.*;
import java.util.stream.Stream;

// TODO: Clean this mess
public abstract class CompileUtils {

    /**
     * Creates a new error.
     * @param s
     *          The error message
     * @param lineIndex
     *          The line of said error
     */
    protected void newError(String s, int lineIndex) {
        System.err.println("["+getClass().getSimpleName()+"] Error at line "+lineIndex+": "+s); // TODO: Collect errors
    }

    /**
     * Removes all the starting space inside the argument
     * @param l
     * @return
     */
    public static String trimStartingSpace(String l) {
        while(l.startsWith(" ")) {
            l = l.substring(1);
        }
        while(l.startsWith("\t")) {
            l = l.substring(1);
        }
        return l;
    }

    public static String readUntilNot(char[] array, int start, char... seeked) {
        StringBuilder builder = new StringBuilder();
        for(int i = start;i<array.length;i++) {
            if(!contains(seeked, array[i]))
                break;
            else
                builder.append(array[i]);
        }
        return builder.toString();
    }

    public static boolean contains(char[] array, char elem) {
        for (char c : array) {
            if (c == elem)
                return true;
        }
        return false;
    }

    /**
     * Reads the possible modifiers present in the text starting from given offset
     * @return
     *         The read modifiers
     */
    public static List<Modifier> readModifiers(Parser parser) {
        List<Modifier> out = new ArrayList<>();
        boolean isValidToken = true;
        while(isValidToken) {
            parser.mark();
            parser.forwardUntilNotList(" ", "\n", "\r");
            String token = parser.forwardToList(" ", "\n");
            isValidToken = false;
            for(ModifierType modifier : ModifierType.values()) {
                if(modifier == ModifierType.ANNOTATION)
                    continue;
                if(modifier.name().toLowerCase().equals(token)) {
                    isValidToken = true;
                    out.add(new Modifier(modifier));
                }
            }

            if(!isValidToken) {
                if(token.startsWith("@")) {
                    String name = Identifier.read(token.toCharArray(), 1).getId();
                    ChoppedAnnotation annotation = new ChoppedAnnotation(name);
                    out.add(new AnnotationModifier(ModifierType.ANNOTATION, annotation));
                    int nameEnd = name.length()+1;
                    if(nameEnd < token.length()) {
                        isValidToken = true;
                        if(token.charAt(nameEnd) == '(') { // We have arguments, yay!
                            String args = readArguments(token.toCharArray(), nameEnd);
                            List<String> argList = new LinkedList<>();
                            String arg;
                            int argumentOffset = 0;
                            do {
                                arg = readSingleArgument(args, argumentOffset, false);
                                argumentOffset += arg.length()+1;
                                if(!arg.isEmpty()) {
                                    argList.add(arg);
                                }
                            } while(!arg.isEmpty());
                            annotation.args.addAll(argList);
                            parser.discardMark();
                            continue;
                        }
                    }
                }
            }
            if(isValidToken) {
                parser.discardMark();
            } else {
                parser.rewind();
            }
        }
        return out;
    }

    /***
     * Reads a single argument inside a list of arguments separated by comas
     * @param constantList
     *
     * @param offset
     * @param isSemiColonValidSeparator
     * @return
     */
    public static String readSingleArgument(String constantList, int offset, boolean isSemiColonValidSeparator) {
        StringBuilder builder = new StringBuilder();
        boolean inString = false;
        boolean inQuote = false;
        boolean escaped = false;
        int unclosedCurlyBrackets = 0;
        int unclosedBrackets = 0;
        char[] chars = constantList.toCharArray();
        iterationLoop: for(int i = offset; i<chars.length;i++) {
            char c = chars[i];
            boolean append = true;
            switch (c) {
                case '"':
                    if (!inQuote && !escaped)
                        inString = !inString;
                    break;

                case '\'':
                    if (!inString && !escaped)
                        inQuote = !inQuote;
                    break;

                case '\\':
                    if(!escaped) {
                        append = false;
                        escaped = true;
                    }
                    break;

                case '(':
                    unclosedBrackets++;
                    break;

                case ')':
                    unclosedBrackets--;
                    break;

                case '{':
                    unclosedCurlyBrackets++;
                    break;

                case '}':
                    unclosedCurlyBrackets--;
                    break;

                case ',':
                    if(unclosedCurlyBrackets == 0 && unclosedBrackets == 0) {
                        break iterationLoop;
                    }
                    break;

                case ';':
                    if(isSemiColonValidSeparator && unclosedCurlyBrackets == 0 && unclosedBrackets == 0) {
                        break iterationLoop;
                    }
                    break;
            }
            if (append)
                builder.append(c);
        }
        return builder.toString();
    }

    public static String readUntilInsnEnd(char[] chars, int offset) {
        StringBuilder builder = new StringBuilder();

        boolean inString = false;
        boolean inQuote = false;
        boolean escaped = false;
        int unclosedBrackets = 0;
        finalLoop: for(int i = offset;i<chars.length;i++) {
            char c = chars[i];
            boolean append = true;
            switch (c) {
                case '"':
                    if(!inQuote && !escaped)
                        inString = !inString;
                    break;

                case '{':
                    if(!inQuote && !inString)
                        unclosedBrackets++;
                    break;

                case '}':
                    if(!inQuote && !inString)
                        unclosedBrackets--;

                    break;

                case '\'':
                    if(!inString && !escaped)
                        inQuote = !inQuote;
                    break;

                case '\\':
                    if(!escaped) {
                        append = false;
                        escaped = true;
                    }
                    break;

                case ';':
                    if(!inQuote && !inString && unclosedBrackets == 0)
                        break finalLoop;
            }
            if(append)
                builder.append(c);
        }
        return builder.toString();
    }

    public static String readArguments(char[] chars, int offset) {
        StringBuilder builder = new StringBuilder();

        boolean inString = false;
        boolean inQuote = false;
        int unclosedBrackets = 1;
        boolean escaped = false;
        finalLoop: for(int i = offset+1;i<chars.length;i++) {
            char c = chars[i];
            boolean append = true;
            switch (c) {
                case '(':
                    unclosedBrackets++;
                    break;

                case ')':
                    unclosedBrackets--;
                    if(unclosedBrackets == 0) {
                        break finalLoop;
                    }
                    break;

                case '"':
                    if(!inQuote && !escaped)
                        inString = !inString;
                    break;

                case '\'':
                    if(!inString && !escaped)
                        inQuote = !inQuote;
                    break;

                case '\\':
                    if(!escaped) {
                        append = false;
                        escaped = true;
                    }
                    break;
            }
            if(append)
                builder.append(c);
        }
        return builder.toString();
    }

    public static String readCodeblock(char[] chars, int codeStart) {
        StringBuilder methodSource = new StringBuilder();
        int unclosedBrackets = 1;
        boolean inString = false;
        boolean inQuote = false;
        boolean escaped = false;
        int j = codeStart;
        bracketLoop: for(;j<chars.length;j++) {
            char c = chars[j];
            boolean append = true;
            switch (c) {
                case '{':
                    if(!inQuote)
                        unclosedBrackets++;
                    break;

                case '}':
                    if(!inQuote) {
                        unclosedBrackets--;
                        if (unclosedBrackets == 0) {
                            break bracketLoop;
                        }
                    }
                    break;

                case '"':
                    if(!inQuote && !escaped)
                        inString = !inString;
                    if(escaped)
                        escaped = false;
                    break;

                case '\'':
                    if(!inString && !escaped)
                        inQuote = !inQuote;
                    if(escaped)
                        escaped = false;
                    break;

                case '\\':
                    if(!escaped) {
                        append = false;
                        escaped = true;
                    }
                    else
                        escaped = false;
                    break;
            }
            if(append)
                methodSource.append(c);
        }
        return methodSource.toString();
    }

    public static String readOperator(Parser parser) {
        String read = readOperator(parser.getData().toCharArray(), parser.getPosition());
        if(read != null) {
            parser.forward(read.length());
        }
        return read;
    }

    public static String readOperator(char[] chars, int offset) {
        List<EnumOperators> operators = new LinkedList<>();
        Collections.addAll(operators, EnumOperators.values());
        operators.remove(EnumOperators.UNARY_MINUS);
        operators.remove(EnumOperators.UNARY_PLUS);
        operators.remove(EnumOperators.CAST);
        String end = new String(chars, offset, chars.length-offset);
        Iterator<EnumOperators> iterator = operators.iterator();
        while(iterator.hasNext()) {
            EnumOperators operator = iterator.next();
            if(operator.raw().length() == chars.length-offset) { // exact size
                if(operator.raw().equals(end)) {
                    return operator.raw();
                } else {
                    iterator.remove();
                }
            } else if(operator.raw().length() > chars.length-offset) { // too long, get out
                iterator.remove();
            } else { // smaller, let's read it
                if(!end.startsWith(operator.raw())) { // absolutely not here, skip
                    iterator.remove();
                }
            }
        }
        Stream<EnumOperators> sortedStream = operators.stream()
                .sorted((a, b) -> -Integer.compare(a.raw().length(), b.raw().length()));
        Optional<EnumOperators> operator = sortedStream
                .findFirst();
        if(operator.isPresent()) {
            return operator.get().raw();
        }
        return null;
    }

}
