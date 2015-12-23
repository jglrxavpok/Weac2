package org.jglr.weac.parse;

import java.util.List;

public class WeacParsedClass {

    public int startingLine = -1;
    public EnumClassTypes classType;
    public List<WeacParsedField> fields;
    public List<WeacParsedMethod> methods;
    public String motherClass;
    public List<String> interfacesImplemented;
}
