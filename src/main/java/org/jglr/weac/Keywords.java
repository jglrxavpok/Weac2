package org.jglr.weac;

/**
 * List of reserved keywords in WeaC
 */
public enum Keywords {
    CLASS, STRUCT, PUBLIC, PROTECTED, PRIVATE, IMPORT, AS, RETURN, THIS, SUPER, OBJECT, MIXIN, NEW;

    private final String stringRepresentation;

    Keywords() {
        stringRepresentation = name().toLowerCase();
    }

    Keywords(String name) {
        stringRepresentation = name;
    }

    public String toString() {
        return stringRepresentation;
    }
}
