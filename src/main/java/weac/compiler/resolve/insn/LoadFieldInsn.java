package weac.compiler.resolve.insn;

import weac.compiler.utils.WeacType;

public class LoadFieldInsn extends ResolvedInsn {
    private final String fieldName;
    private final WeacType owner;
    private final WeacType type;
    private final boolean isStatic;

    public LoadFieldInsn(String fieldName, WeacType owner, WeacType type, boolean isStatic) {
        super(LOAD_FIELD);
        this.fieldName = fieldName;
        this.owner = owner;
        this.type = type;
        this.isStatic = isStatic;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        return super.toString()+" "+fieldName+" of "+owner+(isStatic ? " (static)" : "");
    }

    public boolean isStatic() {
        return isStatic;
    }

    public WeacType getType() {
        return type;
    }

    public WeacType getOwner() {
        return owner;
    }
}
