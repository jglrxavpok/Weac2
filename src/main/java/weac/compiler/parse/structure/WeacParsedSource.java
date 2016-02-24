package weac.compiler.parse.structure;

import weac.compiler.utils.WeacImport;

import java.util.List;

public class WeacParsedSource {

    public String sourceCode;

    public String packageName;
    public List<WeacImport> imports;
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
                System.out.println("import "+i.importedType+" as "+i.usageName);
            } else {
                System.out.println("import "+i.importedType);
            }
        });
        classes.forEach(WeacParsedClass::echo);
        System.out.println("[===END===]");
    }
}
