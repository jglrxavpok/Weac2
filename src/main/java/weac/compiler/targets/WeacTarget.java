package weac.compiler.targets;

import weac.compiler.targets.compile.TargetCompiler;

public interface WeacTarget {

    String getHumanReadableName();

    String getIdentifier();

    TargetCompiler newCompiler();
}
