package org.jglr.weac;

import org.jglr.weac.utils.WeacModifier;

import java.util.List;

/**
 * A phase in the compilation of the source code
 */
public class WeacCompilePhase {

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
            String token = readUntil(chars, offset, ' ');
            isValidToken = false;
            for(WeacModifier modifier : WeacModifier.values()) {
                if(modifier.name().toLowerCase().equals(token)) {
                    isValidToken = true;
                    out.add(modifier);
                }
            }
            if(isValidToken)
                offset += token.length()+readUntilNot(chars, offset+token.length(), ' ', '\n').length();
        }
        return offset-start;
    }
}
