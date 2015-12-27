package org.jglr.weac.parse.structure;

/**
 * Represents a simple import statement
 */
public class WeacParsedImport {

    /**
     * The returnType to import (with the full name)
     */
    public String importedType;

    /**
     * The name used inside the code, if renamed
     */
    public String usageName;
}
