package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class StoreFieldInsn extends ResolvedInsn {
    private final String name;
    private final WeacType type;
    private final boolean isStatic;

    public StoreFieldInsn(String name, WeacType type, boolean isStatic) {
        super(STORE_FIELD);
        this.name = name;
        this.type = type;
        this.isStatic = isStatic;
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

    public boolean isStatic() {
        return isStatic;
    }
}
