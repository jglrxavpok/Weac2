package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class StoreFieldInsn extends ResolvedInsn {
    private final String name;
    private final WeacType owner;
    private final WeacType type;
    private final boolean isStatic;

    public StoreFieldInsn(String name, WeacType owner, WeacType type, boolean isStatic) {
        super(STORE_FIELD);
        this.name = name;
        this.owner = owner;
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
        return super.toString()+" "+name+" "+type+" (in "+owner+")";
    }

    public boolean isStatic() {
        return isStatic;
    }

    public WeacType getOwner() {
        return owner;
    }
}
