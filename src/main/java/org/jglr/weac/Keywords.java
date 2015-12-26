package org.jglr.weac;

public enum Keywords {
    CLASS, STRUCT, PUBLIC, PROTECTED, PRIVATE, IMPORT, AS, RETURN, THIS, SUPER, OBJECT;

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
