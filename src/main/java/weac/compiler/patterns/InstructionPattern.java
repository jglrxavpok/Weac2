package weac.compiler.patterns;

import weac.compiler.utils.Instruction;

public abstract class InstructionPattern<InsnType extends Instruction> extends Pattern<InsnType, Integer> {

    public abstract int[] getOpcodes();

    @Override
    public Integer[] getCategories() {
        int[] ops = getOpcodes();
        Integer[] categories = new Integer[ops.length];
        for(int i = 0;i<categories.length;i++) {
            categories[i] = ops[i];
        }
        return categories;
    }

    protected boolean isValid(InsnType insn, Integer expectedCode, int index) {
        return insn.getOpcode() == expectedCode;
    }
}
