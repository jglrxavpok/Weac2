package weac.compiler.targets;

public interface WeacTarget {

    String getHumanReadableName();

    String getIdentifier();

    TargetCompiler newCompiler();
}
