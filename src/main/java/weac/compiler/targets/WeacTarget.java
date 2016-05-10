package weac.compiler.targets;

import weac.compiler.resolve.Resolver;
import weac.compiler.resolve.TypeResolver;

public interface WeacTarget {

    String getHumanReadableName();

    String getIdentifier();

    TargetCompiler newCompiler();

    TypeResolver newTypeResolver(Resolver resolver);
}
