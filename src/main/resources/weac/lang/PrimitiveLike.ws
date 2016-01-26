package weac.lang

annotation PrimitiveLike {
    private String primitiveType;

    public PrimitiveLike(String primitiveType) {
        this.primitiveType = primitiveType;
    }

    public String getPrimitiveType() {
        return primitiveType;
    }
}