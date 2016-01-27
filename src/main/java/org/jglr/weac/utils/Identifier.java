package org.jglr.weac.utils;

import org.jglr.weac.Keywords;

import java.util.List;

/**
 * A WeaC identifier, used for types, variable and method names
 */
public class Identifier {

    public static final char[] allowedCharacters = {'<', '>', '*', '[', ']'};

    /**
     * The raw id
     */
    private final String id;

    /**
     * Used when having a problem to read the identifier
     */
    public static final Identifier INVALID = new Identifier("");

    public static final Identifier VOID = new Identifier("Void");
    private final boolean fullName;

    /**
     * Creates a new instance of {@link Identifier}
     * @param id
     *          The raw id
     */
    public Identifier(String id) {
        this(id, false);
    }

    public Identifier(String id, boolean fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return getId();
    }

    /**
     * Returns <code>true</code> if this identifier is a valid one. Returns <code>false</code> otherwise
     * @return
     *          <code>true</code> if this identifier is a valid one, <code>false</code> otherwise
     */
    public boolean isValid() {
        return isValid(id, fullName);
    }

    /**
     * Returns <code>true</code> if the identifier is a valid one. Returns <code>false</code> otherwise
     * @return
     *          <code>true</code> if the identifier is a valid one, <code>false</code> otherwise
     */
    public static boolean isValid(String potientialID) {
        return isValid(potientialID, false);
    }

    public static boolean isValid(String potientialID, boolean fullName) {
        for(Keywords w : Keywords.values()) {
           if(w.toString().toLowerCase().equals(potientialID)) {
               return false;
           }
        }
        if(potientialID.isEmpty())
            return false;
        if(!Character.isJavaIdentifierStart(potientialID.charAt(0))) {
            return false;
        }
        for(int i = 1;i<potientialID.length();i++) {
            char c = potientialID.charAt(i);
            if(!Character.isJavaIdentifierPart(c) && !isAllowedCharacter(c, fullName)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllowedCharacter(char c, boolean fullName) {
        if(c == '.' && fullName)
            return true;
        for(char allowed : allowedCharacters) {
            if(c == allowed) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads an identifier from text
     * @param chars
     *              The source code characters
     * @param start
     *              The offset at which to start reading in the source code
     * @return
     *          The read identifier or {@link #INVALID}
     *
     * @see java.lang.Character#isJavaIdentifierPart(char)
     * @see java.lang.Character#isJavaIdentifierStart(char)
     */
    public static Identifier read(char[] chars, int start) {
        StringBuilder builder = new StringBuilder();
        if(!Character.isJavaIdentifierStart(chars[start])) {
            return INVALID;
        }
        builder.append(chars[start]);
        for(int i = start+1;i<chars.length;i++) {
            char c = chars[i];
            if(!Character.isJavaIdentifierPart(c) && !isAllowedCharacter(c, false)) {
                break;
            }
            builder.append(c);
        }
        return new Identifier(builder.toString());
    }

    /**
     * Returns the raw id
     * @return
     *          Raw ID
     */
    public String getId() {
        return id;
    }
}
