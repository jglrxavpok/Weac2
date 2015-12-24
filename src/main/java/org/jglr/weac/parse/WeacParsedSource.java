package org.jglr.weac.parse;

import java.util.List;

public class WeacParsedSource {

    public String sourceCode;

    public String packageName;
    public List<WeacParsedImport> imports;
    public List<WeacParsedClass> classes;

    /**
     * Debug
     */
    @Deprecated
    public void echo() {
        System.out.println("[=========]");
        System.out.println("package "+packageName);
        imports.forEach(i -> {
            if(i.usageName != null) {
                System.out.println(i.importedType+" as "+i.usageName);
            } else {
                System.out.println(i.importedType);
            }
        });
        classes.forEach(WeacParsedClass::echo);
        System.out.println("[===END===]");
    }
}
