package org.jglr.weac.utils;

public class WeacType {

    public static final WeacType VOID_TYPE = new WeacType("void");
    private final Identifier identifier;
    private boolean isArray;
    private boolean isValid;
    private boolean isPointer;
    private WeacType pointerType;
    private WeacType genericParameter;
    private WeacType arrayType;
    private final WeacType coreType;

    public WeacType(String id) {
        this(new Identifier(id));
    }

    public WeacType(Identifier identifier) {
        this.identifier = identifier;
        if(identifier.isValid()) {
            isValid = true;
            String id = identifier.getId();
            if(id.endsWith("*")) {
                isPointer = true;
                id = id.substring(0, id.length()-1); // removes the '*' from the id
                pointerType = new WeacType(id);
                coreType = pointerType.getCoreType();
            } else if(id.endsWith("[]")) {
                isArray = true;
                id = id.substring(0, id.length()-2); // removes the '[]' from the id
                arrayType = new WeacType(id);
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
                    genericParameter = new WeacType(id.substring(id.indexOf('<')+1, id.lastIndexOf('>')));
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
