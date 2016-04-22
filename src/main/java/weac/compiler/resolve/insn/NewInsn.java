package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class NewInsn extends ResolvedInsn {
    private final WeacType type;

    public NewInsn(WeacType type) {
        super(ResolveOpcodes.NEW);
        this.type = type;
    }

    public WeacType getType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString()+" "+type;
    }
}
