package weac.compiler.chop.structure;

import weac.compiler.utils.Import;

import java.util.List;

public class ChoppedSource {

    public String sourceCode;

    public String packageName;
    public List<Import> imports;
    public List<ChoppedClass> classes;
    public String fileName;
    public String version;
    public String target;

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
        classes.forEach(ChoppedClass::echo);
        System.out.println("[===END===]");
    }
}
