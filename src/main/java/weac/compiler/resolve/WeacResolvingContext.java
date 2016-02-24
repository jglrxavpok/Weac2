package weac.compiler.resolve;

import weac.compiler.precompile.structure.WeacPrecompiledClass;
import weac.compiler.precompile.structure.WeacPrecompiledSource;

public class WeacResolvingContext {

    private final WeacPrecompiledSource source;
    private final WeacPrecompiledClass[] sideClasses;

    public WeacResolvingContext(WeacPrecompiledSource source, WeacPrecompiledClass[] sideClasses) {
        this.source = source;
        this.sideClasses = sideClasses;
    }

    public WeacPrecompiledClass[] getSideClasses() {
        return sideClasses;
    }

    public WeacPrecompiledSource getSource() {
        return source;
    }
}
