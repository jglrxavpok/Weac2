package weac.compiler.targets.jvm;

import weac.compiler.targets.jvm.compile.JVMCompiler;
import weac.compiler.targets.TargetCompiler;
import weac.compiler.targets.WeacTarget;

public class JVMTarget implements WeacTarget {

    @Override
    public String getHumanReadableName() {
        return "Java Virtual Machine";
    }

    @Override
    public String getIdentifier() {
        return "jvm";
    }

    @Override
    public TargetCompiler newCompiler() {
        return new JVMCompiler();
    }
}
