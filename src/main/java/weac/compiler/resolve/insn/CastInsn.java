package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class CastInsn extends ResolvedInsn {
    private final WeacType from;
    private final WeacType to;

    public CastInsn(WeacType from, WeacType to) {
        super(CAST);
        this.from = from;
        this.to = to;
    }

    public WeacType getFrom() {
        return from;
    }

    public WeacType getTo() {
        return to;
    }

    @Override
    public String toString() {
        return super.toString()+" from "+from+" to "+to;
    }
}
