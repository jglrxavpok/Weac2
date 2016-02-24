package weac.compiler.precompile.insn;

import weac.compiler.precompile.WeacLabel;

public class WeacIfNotJumpInsn extends WeacPrecompiledInsn {
    private final WeacLabel jumpTo;

    public WeacIfNotJumpInsn(WeacLabel jumpTo) {
        super(JUMP_IF_NOT_TRUE);
        this.jumpTo = jumpTo;
    }

    public WeacLabel getJumpTo() {
        return jumpTo;
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj))
            return false;
        if(obj instanceof WeacPrecompiledInsn) {
            WeacIfNotJumpInsn casted = ((WeacIfNotJumpInsn) obj);
            return casted.jumpTo.equals(jumpTo);
        }
        return false;
    }
}
