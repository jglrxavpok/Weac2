package org.jglr.weac.utils;

public class WeacType {

    public static final WeacType VOID_TYPE = new WeacType("void", false);
    public static final WeacType BOOLEAN_TYPE = new WeacType("weac.lang.Boolean", true);
    public static final WeacType DOUBLE_TYPE = new WeacType("weac.lang.Double", true);
    public static final WeacType FLOAT_TYPE = new WeacType("weac.lang.Float", true);
    public static final WeacType INTEGER_TYPE = new WeacType("weac.lang.Int", true);
    public static final WeacType LONG_TYPE = new WeacType("weac.lang.Long", true);
    public static final WeacType SHORT_TYPE = new WeacType("weac.lang.Short", true);
    public static final WeacType CHAR_TYPE = new WeacType("weac.lang.Char", true);
    private final Identifier identifier;
    private boolean isArray;
    private boolean isValid;
    private boolean isPointer;
    private WeacType pointerType;
    private WeacType genericParameter;
    private WeacType arrayType;
    private final WeacType coreType;

    public WeacType(String id, boolean fullName) {
        this(new Identifier(id, fullName));
    }

    public WeacType(Identifier identifier) {
        this.identifier = identifier;
        if(identifier.isValid()) {
            isValid = true;
            String id = identifier.getId();
            if(id.endsWith("*")) {
                isPointer = true;
                id = id.substring(0, id.length()-1); // removes the '*' from the id
                pointerType = new WeacType(id, true);
                coreType = pointerType.getCoreType();
            } else if(id.endsWith("[]")) {
                isArray = true;
                id = id.substring(0, id.length()-2); // removes the '[]' from the id
                arrayType = new WeacType(id, true);
                coreType = arrayType.getCoreType();
            } else {
                coreType = this;
            }

            if(id.contains("<")) {
                int countLeft = count(id, '<');
                int countRight = count(id, '>');
                if(countLeft != countRight) { // unmatched
                    isValid = false;
                } else {
                    genericParameter = new WeacType(id.substring(id.indexOf('<')+1, id.lastIndexOf('>')), true);
                }
            } else if(id.contains(">")) { // unmatched
                isValid = false;
            }
        } else {
            isValid = false;
            coreType = VOID_TYPE;
        }
    }

    private int count(String id, char c) {
        int count = 0;
        for(char c1 : id.toCharArray()) {
            if(c1 == c)
                count++;
        }
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WeacType) {
            WeacType other = ((WeacType) obj);
            if(other.getIdentifier().getId().equals(identifier.getId()))
                return true;
        }
        return obj == this;
    }

    public boolean isValid() {
        return isValid;
    }

    public boolean isPointer() {
        return isPointer;
    }

    public WeacType getPointerType() {
        return pointerType;
    }

    public WeacType getGenericParameter() {
        return genericParameter;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public WeacType getArrayType() {
        return arrayType;
    }

    public boolean isArray() {
        return isArray;
    }

    public WeacType getCoreType() {
        return coreType;
    }
}
