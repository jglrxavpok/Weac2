package org.jglr.weac.parse;

public class WeacParsedMethod {

    public int startingLine = -1;
    public String type;
    public String name;
    public String[] argumentNames;
    public String[] argumentTypes;

    public String methodSource;
    public boolean isAbstract;

    public String access = "public";
}
