package org.jglr.weac.resolve.insn;

import org.jglr.weac.utils.WeacType;

public class WeacStoreFieldInsn extends WeacResolvedInsn {
    private final String name;
    private final WeacType type;

    public WeacStoreFieldInsn(String name, WeacType type) {
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
