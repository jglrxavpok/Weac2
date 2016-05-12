package weac.compiler.utils;

import weac.compiler.CompileUtils;
import weac.compiler.targets.jvm.JVMWeacTypes;

public class WeacType {

    public static final WeacType AUTO = new WeacType(JVMWeacTypes.OBJECT_TYPE, "var", true);

    private final WeacType superType;
    private final Identifier identifier;
    private boolean isGeneric;
    private boolean isArray;
    private boolean isValid;
    private boolean isPointer;
    private WeacType pointerType;
    private WeacType[] genericParameters;
    private WeacType arrayType;
    private WeacType coreType;

    public WeacType(WeacType superType, String id, boolean fullName) {
        this(superType, new Identifier(id, fullName));
    }

    public WeacType(WeacType superType, Identifier identifier) {
        this.superType = superType;
        this.identifier = identifier;
        if(identifier.isValid()) {
            isValid = true;
            String id = identifier.getId();
            if(id.endsWith("*")) {
                isPointer = true;
                id = id.substring(0, id.length()-1); // removes the '*' from the id
                pointerType = new WeacType(JVMWeacTypes.POINTER_TYPE, id, true);
                coreType = pointerType.getCoreType();
            } else if(id.endsWith("[]")) {
                isArray = true;
                id = id.substring(0, id.length()-2); // removes the '[]' from the id
                arrayType = new WeacType(JVMWeacTypes.ARRAY_TYPE, id, true);
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
                    isGeneric = true;
                    int start = id.indexOf('<');
                    String genericParametersRaw = id.substring(start+1, id.lastIndexOf('>'));
                    String[] params = genericParametersRaw.split(",");
                    genericParameters = new WeacType[params.length];
                    for (int i = 0; i < genericParameters.length; i++) {
                        genericParameters[i] = new WeacType(JVMWeacTypes.OBJECT_TYPE, CompileUtils.trimStartingSpace(params[i]), true);
                    }
                    coreType = new WeacType(JVMWeacTypes.OBJECT_TYPE, id.substring(0, start), true).getCoreType();
                }
            } else if(id.contains(">")) { // unmatched
                isValid = false;
            }
        } else {
            isValid = false;
            coreType = JVMWeacTypes.VOID_TYPE;
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

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
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

    public WeacType[] getGenericParameters() {
        return genericParameters;
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

    public boolean isGeneric() {
        return isGeneric;
    }

    public WeacType getCoreType() {
        return coreType;
    }

    public WeacType getSuperType() {
        return superType;
    }

    public boolean isPrimitive() {
        return superType != null && superType.equals(JVMWeacTypes.PRIMITIVE_TYPE);
    }

    @Override
    public String toString() {
        return identifier.toString();
    }
}
