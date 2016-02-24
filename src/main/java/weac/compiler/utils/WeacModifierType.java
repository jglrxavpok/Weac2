package weac.compiler.utils;

public enum WeacModifierType {
    PUBLIC(true, false), PRIVATE(true, false), PROTECTED(true, false), ABSTRACT(false, true), MIXIN(false, true), ANNOTATION(false, true),
    COMPILERSPECIAL(false, true), FINAL(false, true);

    private final boolean accessModifier;
    private final boolean roleModifier;

    WeacModifierType(boolean accessModifier, boolean roleModifier) {
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
