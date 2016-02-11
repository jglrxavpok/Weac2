package org.jglrxavpok.weac.resolve.insn;

public class WeacLoadCharInsn extends WeacResolvedInsn {

    private final char number;

    public WeacLoadCharInsn(char number) {
        super(LOAD_CHARACTER_CONSTANT);
        this.number = number;
    }

    public char getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return super.toString()+" "+number;
    }
}
