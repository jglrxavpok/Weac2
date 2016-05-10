package weac.compiler.precompile.structure;

import weac.compiler.resolve.TypeResolver;
import weac.compiler.targets.WeacTarget;
import weac.compiler.utils.Import;

import java.util.List;

public class PrecompiledSource {

    public List<PrecompiledClass> classes;
    public List<Import> imports;
    public String packageName;
    public String fileName;
    public WeacTarget target;
}
