package org.jglr.weac.parse;

/**
 * The different class types possible in WeaC
 */
public enum EnumClassTypes {
    /**
     * A class
     */
    CLASS,

    /**
     * An interface
     */
    INTERFACE,

    /**
     * An enum
     */
    ENUM,

    /**
     * A struct-like class. A struct in WeaC cannot have methods but can have default values for its content.
     */
    STRUCT,

    /**
     * A singleton.
     */
    OBJECT,

    /**
     * An annotation
     */
    ANNOTATION,
}
