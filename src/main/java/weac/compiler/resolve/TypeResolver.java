package weac.compiler.resolve;

import weac.compiler.precompile.structure.PrecompiledClass;
import weac.compiler.utils.Identifier;
import weac.compiler.utils.WeacType;

public abstract class TypeResolver {

    public abstract WeacType resolveType(Identifier id, ResolvingContext context);

    public abstract boolean isCastable(PrecompiledClass from, PrecompiledClass to, ResolvingContext context);

    public abstract PrecompiledClass findClass(String inter, ResolvingContext context);

    public abstract WeacType findResultType(WeacType left, WeacType right, ResolvingContext context);
}
