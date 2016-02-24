package weac.compiler.precompile.insn;

public class WeacCreateArray extends WeacPrecompiledInsn {
    private final int length;
    private final String type;

    public WeacCreateArray(int length, String type) {
        super(CREATE_ARRAY);
        this.length = length;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return super.toString() + ' ' + length+" ("+type+")";
    }

    @Override
    public boolean equals(Object obj) {
        if(!super.equals(obj))
            return false;
        if(obj instanceof WeacPrecompiledInsn) {
            WeacCreateArray casted = ((WeacCreateArray) obj);
            return casted.getType().equals(type) && casted.getLength() == length;
        }
        return false;
    }
}
