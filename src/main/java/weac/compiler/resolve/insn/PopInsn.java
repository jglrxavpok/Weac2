package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class PopInsn extends ResolvedInsn {
    private final WeacType type;

    public PopInsn(WeacType type) {
        super(POP);
        this.type = type;
    }

    public WeacType getRemovedType() {
        return type;
    }
}
