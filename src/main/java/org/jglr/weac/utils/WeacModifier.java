package org.jglr.weac.utils;

public enum WeacModifier {
    PUBLIC(true, false), PRIVATE(true, false), PROTECTED(true, false), ABSTRACT(false, true), MIXIN(false, true);

    private final boolean accessModifier;
    private final boolean roleModifier;

    WeacModifier(boolean accessModifier, boolean roleModifier) {
        this.accessModifier = accessModifier;
        this.roleModifier = roleModifier;
    }

    public boolean isRoleModifier() {
        return roleModifier;
    }

    public boolean isAccessModifier() {
        return accessModifier;
    }
}
