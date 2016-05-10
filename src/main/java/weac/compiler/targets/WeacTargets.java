package weac.compiler.targets;

import weac.compiler.targets.jvm.JVMTarget;

public enum WeacTargets {

    JVM(new JVMTarget());

    private final WeacTarget target;

    WeacTargets(JVMTarget target) {
        this.target = target;
    }

    public WeacTarget value() {
        return target;
    }
}
