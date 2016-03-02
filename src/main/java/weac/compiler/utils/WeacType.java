package weac.compiler.utils;

import weac.compiler.CompileUtils;

public class WeacType {

    public static final WeacType JOBJECT_TYPE = new WeacType(null, "java.lang.Object", true);
    public static final WeacType OBJECT_TYPE = new WeacType(JOBJECT_TYPE, Constants.BASE_CLASS, true);
    public static final WeacType PRIMITIVE_TYPE = new WeacType(OBJECT_TYPE, "weac.lang.Primitive", true);
    public static final WeacType VOID_TYPE = new WeacType(JOBJECT_TYPE, "weac.lang.Void", false);
    public static final WeacType BOOLEAN_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Boolean", true);
    public static final WeacType BYTE_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Byte", true);
    public static final WeacType DOUBLE_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Double", true);
    public static final WeacType FLOAT_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Float", true);
    public static final WeacType INTEGER_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Int", true);
    public static final WeacType LONG_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Long", true);
    public static final WeacType SHORT_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Short", true);
    public static final WeacType CHAR_TYPE = new WeacType(PRIMITIVE_TYPE, "weac.lang.Char", true);

    public static final WeacType STRING_TYPE = new WeacType(JOBJECT_TYPE, "java.lang.String", true);

    public static final WeacType POINTER_TYPE = new WeacType(OBJECT_TYPE, "$$Pointer", true);
    public static final WeacType ARRAY_TYPE = new WeacType(OBJECT_TYPE, "$$Array", true);
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
                pointerType = new WeacType(POINTER_TYPE, id, true);
                coreType = pointerType.getCoreType();
            } else if(id.endsWith("[]")) {
                isArray = true;
                id = id.substring(0, id.length()-2); // removes the '[]' from the id
                arrayType = new WeacType(ARRAY_TYPE, id, true);
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
                        genericParameters[i] = new WeacType(OBJECT_TYPE, CompileUtils.trimStartingSpace(params[i]), true);
                    }
                    coreType = new WeacType(OBJECT_TYPE, id.substring(0, start), true).getCoreType();
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

    @Override
    public String toString() {
        return identifier.toString();
    }
}