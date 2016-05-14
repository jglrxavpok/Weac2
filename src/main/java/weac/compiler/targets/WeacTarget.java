package weac.compiler.targets;

import weac.compiler.resolve.NativeCodeResolver;
import weac.compiler.resolve.Resolver;
import weac.compiler.resolve.ResolvingContext;
import weac.compiler.resolve.TypeResolver;
import weac.compiler.utils.WeacType;

public interface WeacTarget {

    String getHumanReadableName();

    String getIdentifier();

    TargetCompiler newCompiler();

    TypeResolver newTypeResolver(Resolver resolver);

    NativeCodeResolver newNativeCodeResolver(Resolver resolver, WeacType selfType, ResolvingContext context);
}
