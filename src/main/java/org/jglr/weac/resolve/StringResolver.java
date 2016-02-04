package org.jglr.weac.resolve;

import org.jglr.weac.WeacCompileUtils;
import org.jglr.weac.resolve.insn.WeacLoadStringInsn;
import org.jglr.weac.resolve.insn.WeacResolvedInsn;

public class StringResolver extends WeacCompileUtils {
    public WeacResolvedInsn resolve(String value) {
        char[] chars = value.toCharArray();
        StringBuilder builder = new StringBuilder();
        int offset = 0;
        while(offset < chars.length) {
            int progress = resolveSingleCharacter(chars, offset, builder);
            offset += progress;
        }
        return new WeacLoadStringInsn(builder.toString());
    }

    public char resolveSingleCharacter(char[] value, int offset) {
        StringBuilder b = new StringBuilder();
        resolveSingleCharacter(value, offset, b);
        return b.charAt(0);
    }

    public int resolveSingleCharacter(char[] value, int offset, StringBuilder out) {
        /*
         * From https://docs.oracle.com/javase/tutorial/java/data/characters.html
         \t	Insert a tab in the text at this point.
         \b	Insert a backspace in the text at this point.
         \n	Insert a newline in the text at this point.
         \r	Insert a carriage return in the text at this point.
         \f	Insert a formfeed in the text at this point.
         \'	Insert a single quote character in the text at this point.
         \"	Insert a double quote character in the text at this point.
         \\	Insert a backslash character in the text at this point.
         */
        int start = offset;
        boolean escaped = false;
        for(;offset<value.length;offset++) {
            char c = value[offset];
            if(escaped) {
                if(c == '\\') {
                    out.append('\\');
                    break;
                } else if(c == 't') {
                    out.append('\t');
                    break;
                } else if(c == 'b') {
                    out.append('\b');
                    break;
                } else if(c == 'n') {
                    out.append('\n');
                    break;
                } else if(c == 'r') {
                    out.append('\r');
                    break;
                } else if(c == 'f') {
                    out.append('\f');
                    break;
                } else if(c == '\'') {
                    out.append('\'');
                    break;
                } else if(c == '\"') {
                    out.append('\"');
                    break;
                } else {
                    newError("Invalid escaped character: \\"+c, -1); // TODO: line
                }
            } else {
                if(c == '\\') {
                    escaped = true;
                } else {
                    out.append(c);
                    break;
                }
            }
        }
        return (offset-start)+1;
    }
}
