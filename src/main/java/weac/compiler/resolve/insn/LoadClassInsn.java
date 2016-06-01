package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class LoadClassInsn extends ResolvedInsn {
    private final WeacType classToLoad;

    public LoadClassInsn(WeacType owner) {
        super(LOAD_CLASS);
        this.classToLoad = owner;
    }

    public WeacType getClassToLoad() {
        return classToLoad;
    }

    @Override
    public String toString() {
        return super.toString()+" "+classToLoad;
    }
}
