package weac.compiler.chop;

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
     * A data class. A data class in WeaC cannot define methods but can have default values for its fields. Accessors (get/set), equality check and toString() methods are provided by the compiler
     */
    DATA,

    /**
     * A singleton.
     */
    OBJECT,

    /**
     * An annotation
     */
    ANNOTATION,
}
