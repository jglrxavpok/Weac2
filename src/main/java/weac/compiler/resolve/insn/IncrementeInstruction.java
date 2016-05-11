package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class IncrementeInstruction extends ResolvedInsn {
    private final WeacType valueType;
    private final int amount;

    public IncrementeInstruction(WeacType type, int amount) {
        super(amount > 0 ? INCREMENT : DECREMENT);
        this.valueType = type;
        this.amount = Math.abs(amount);
    }

    public int getAmount() {
        return amount;
    }

    public WeacType getValueType() {
        return valueType;
    }
}
