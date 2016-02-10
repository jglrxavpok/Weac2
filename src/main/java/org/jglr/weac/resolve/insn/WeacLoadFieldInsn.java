package org.jglr.weac.resolve.insn;

import org.jglr.weac.utils.WeacType;

public class WeacLoadFieldInsn extends WeacResolvedInsn {
    private final String fieldName;
    private final WeacType owner;
    private final WeacType type;
    private final boolean isStatic;

    public WeacLoadFieldInsn(String fieldName, WeacType owner, WeacType type, boolean isStatic) {
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
