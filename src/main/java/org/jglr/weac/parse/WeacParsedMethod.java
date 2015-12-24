package org.jglr.weac.parse;

import org.jglr.weac.utils.Identifier;

import java.util.ArrayList;
import java.util.List;

public class WeacParsedMethod {

    public int startingLine = -1;
    public Identifier type;
    public Identifier name;
    public List<Identifier> argumentNames;
    public List<Identifier> argumentTypes;

    public String methodSource;
    public boolean isAbstract;

    public String access = "public";

    public WeacParsedMethod() {
        argumentNames = new ArrayList<>();
        argumentTypes = new ArrayList<>();
    }

    public void echo() {
        System.out.print(access+" "+type+" "+name+"(");
        for(int i = 0;i<argumentNames.size();i++) {
            if(i != 0) {
                System.out.print(", ");
            }
            System.out.print(argumentTypes.get(i));
            System.out.print(" ");
            System.out.print(argumentNames.get(i));
        }
        System.out.println(")");
    }
}
