package weac.compiler.utils;

public class GenericType extends WeacType {

    public GenericType(WeacType type) {
        super(type.getSuperType(), type.getIdentifier());
    }

    public GenericType(String id) {
        super(null, id, false);
    }

    public GenericType(Identifier identifier) {
        super(null, identifier);
    }
}
