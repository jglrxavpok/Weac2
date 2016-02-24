package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class StoreFieldInsn extends ResolvedInsn {
    private final String name;
    private final WeacType type;

    public StoreFieldInsn(String name, WeacType type) {
        super(STORE_FIELD);
        this.name = name;
        this.type = type;
    }

    public WeacType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return super.toString()+" "+name+" "+type;
    }
}
