package org.jglr.weac.parse;

import java.util.List;

public class WeacParsedClass {

    public int startingLine = -1;
    public String name;
    public EnumClassTypes classType;
    public List<WeacParsedField> fields;
    public List<WeacParsedMethod> methods;
    public String motherClass;
    public List<String> interfacesImplemented;
    public String access = "public";

    @Deprecated
    public void echo() {
        System.out.print(access+" "+classType.name().toLowerCase()+" "+name);
        if(motherClass != null) {
            System.out.print(" > "+motherClass);
            if(!interfacesImplemented.isEmpty())
                System.out.print(" + ");
        } else {
            if(!interfacesImplemented.isEmpty())
                System.out.print(" > ");
        }

        for(String interfaceImpl : interfacesImplemented) {
            System.out.print(interfaceImpl);
            System.out.print(" + ");
        }
        System.out.println();
    }
}
