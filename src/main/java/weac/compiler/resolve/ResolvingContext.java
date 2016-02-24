package weac.compiler.resolve;

import weac.compiler.precompile.structure.PrecompiledClass;
import weac.compiler.precompile.structure.PrecompiledSource;

public class ResolvingContext {

    private final PrecompiledSource source;
    private final PrecompiledClass[] sideClasses;

    public ResolvingContext(PrecompiledSource source, PrecompiledClass[] sideClasses) {
        this.source = source;
        this.sideClasses = sideClasses;
    }

    public PrecompiledClass[] getSideClasses() {
        return sideClasses;
    }

    public PrecompiledSource getSource() {
        return source;
    }
}
