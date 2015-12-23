package org.jglr.weac;

public enum Keywords {
    CLASS, STRUCT, PUBLIC, PROTECTED, PRIVATE, IMPORT, AS, RETURN, THIS, SUPER;

    private final String stringRepresentation;

    private Keywords() {
        stringRepresentation = name().toLowerCase();
    }

    private Keywords(String name) {
        stringRepresentation = name;
    }

    public String toString() {
        return stringRepresentation;
    }
}
