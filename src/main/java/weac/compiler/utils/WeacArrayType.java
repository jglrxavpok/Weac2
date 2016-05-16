package weac.compiler.utils;

public class WeacArrayType extends WeacType {

    public WeacArrayType(WeacType elementType) {
        super(ARRAY_TYPE, elementType.getIdentifier()+"[]", true);
    }
}
