package org.jglr.weac.utils;

import org.jglr.weac.Keywords;

public class Identifier {

    private final String id;

    public static final Identifier INVALID = new Identifier("");

    public Identifier(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getId();
    }

    public boolean isValid() {
        return isValid(id);
    }

    public static boolean isValid(String potientialID) {
        for(Keywords w : Keywords.values()) {
           if(w.toString().toLowerCase().equals(potientialID)) {
               return false;
           }
        }
        if(potientialID.isEmpty())
            return false;
        if(!Character.isJavaIdentifierStart(potientialID.charAt(0)))
            return false;
        for(int i = 1;i<potientialID.length();i++) {
            if(!Character.isJavaIdentifierPart(potientialID.charAt(i)))
                return false;
        }
        return true;
    }

    public static Identifier read(char[] chars, int start) {
        StringBuilder builder = new StringBuilder();
        if(!Character.isJavaIdentifierStart(chars[start])) {
            return INVALID;
        }
        builder.append(chars[start]);
        for(int i = start+1;i<chars.length;i++) {
            char c = chars[i];
            if(!Character.isJavaIdentifierPart(c)) {
                break;
            }
            builder.append(c);
        }
        return new Identifier(builder.toString());
    }

    public String getId() {
        return id;
    }
}
