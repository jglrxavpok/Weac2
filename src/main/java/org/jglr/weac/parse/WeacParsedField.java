package org.jglr.weac.parse;

import org.jglr.weac.utils.Identifier;

public class WeacParsedField {

    public int startingLine = -1;
    public Identifier type;
    public Identifier name;
    public String defaultValue;

    public String access = "public";

    public void echo() {
        if(defaultValue != null)
            System.out.println(type+" "+name+" = "+defaultValue);
        else
            System.out.println(type+" "+name);
    }
}
