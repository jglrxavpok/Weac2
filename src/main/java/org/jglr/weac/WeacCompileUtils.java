package org.jglr.weac;

import org.jglr.weac.utils.AnnotationModifier;
import org.jglr.weac.utils.WeacAnnotation;
import org.jglr.weac.utils.WeacModifier;
import org.jglr.weac.utils.WeacModifierType;

import java.util.List;

public abstract class WeacCompileUtils {

    /**
     * Creates a new error.
     * @param s
     *          The error message
     * @param lineIndex
     *          The line of said error
     */
    protected void newError(String s, int lineIndex) {
        System.err.println("Error at line "+lineIndex+": "+s); // TODO: Collect errors
    }

    /**
     * Extracts a substring from the start to the first space character found.
     * @param arg
     *              The argument
     * @return
     *              The substring
     */
    protected String readUntilSpace(String arg) {
        int end = arg.indexOf(' ');
        if(end < 0)
            end = arg.length();
        return arg.substring(0, end);
    }

    /**
     * Removes all the starting space inside the argument
     * @param l
     * @return
     */
    protected String trimStartingSpace(String l) {
        while(l.startsWith(" ")) {
            l = l.substring(1);
        }
        while(l.startsWith("\t")) {
            l = l.substring(1);
        }
        return l;
    }

    /**
     * Extracts a String from <code>start</code> to <code>end</code>
     * @param array
     *              The characters to extract from
     * @param start
     *              The starting point from which to read
     * @param end
     *              The ending point until which to read
     * @return
     *              The extracted text
     */
    protected String read(char[] array, int start, int end) {
        StringBuilder builder = new StringBuilder();
        for(int i = start;i<end;i++) {
            builder.append(array[i]);
        }
        return builder.toString();
    }

    protected int indexOf(char[] array, int start, char toFind) {
        for(int i = start;i<array.length;i++) {
            if(array[i] == toFind)
                return i;
        }
        return -1;
    }

    protected String readUntilNot(char[] array, int start, char... seeked) {
        StringBuilder builder = new StringBuilder();
        for(int i = start;i<array.length;i++) {
            if(!contains(seeked, array[i]))
                break;
            else
                builder.append(array[i]);
        }
        return builder.toString();
    }

    private boolean contains(char[] array, char elem) {
        for (char c : array) {
            if (c == elem)
                return true;
        }
        return false;
    }

    protected String readUntil(char[] array, int start, char... seeked) {
        StringBuilder builder = new StringBuilder();
        for(int i = start;i<array.length;i++) {
            if(contains(seeked, array[i]))
                break;
            else
                builder.append(array[i]);
        }
        return builder.toString();
    }

    /**
     * Reads the possible modifiers present in the text starting from given offset
     * @param chars
     *                  The characters to read from
     * @param offset
     *                  The offset from which to start the reading
     * @param out
     *                  The list where to store the modifiers
     * @return
     *         The number of read characters
     */
    protected int readModifiers(char[] chars, int offset, List<WeacModifier> out) {
        int start = offset;
        System.out.println(readUntilNot(chars, start, ' ', '\n'));
        offset += readUntilNot(chars, start, ' ', '\n').length();
        boolean isValidToken = true;
        while(isValidToken) {
            String token = readUntil(chars, offset, ' ', '\n');
            isValidToken = false;
            for(WeacModifierType modifier : WeacModifierType.values()) {
                if(modifier == WeacModifierType.ANNOTATION)
                    continue;
                if(modifier.name().toLowerCase().equals(token)) {
                    isValidToken = true;
                    out.add(new WeacModifier(modifier));
                }
            }
            if(!isValidToken) {
                if(token.startsWith("@")) {
                    WeacAnnotation annotation = new WeacAnnotation(token.substring(1));
                    out.add(new AnnotationModifier(WeacModifierType.ANNOTATION, annotation));
                    isValidToken = true;
                    // TODO: read arguments
                }
            }
            if(isValidToken)
                offset += token.length()+readUntilNot(chars, offset+token.length(), ' ', '\n').length();
        }
        return offset-start;
    }

    protected String readSingleArgument(String constantList, int offset, boolean isSemiColonValidSeparator) {
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

    protected String readUntilInsnEnd(char[] chars, int offset) {
        StringBuilder builder = new StringBuilder();

        boolean inString = false;
        boolean inQuote = false;
        boolean escaped = false;
        finalLoop: for(int i = offset;i<chars.length;i++) {
            char c = chars[i];
            boolean append = true;
            switch (c) {
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

                case ';':
                    if(!inQuote && !inString)
                        break finalLoop;
            }
            if(append)
                builder.append(c);
        }
        return builder.toString();
    }

    protected String readArguments(char[] chars, int offset) {
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

    protected String readCodeblock(char[] chars, int codeStart) {
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
                    unclosedBrackets++;
                    break;

                case '}':
                    unclosedBrackets--;
                    if(unclosedBrackets == 0) {
                        break bracketLoop;
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
}
