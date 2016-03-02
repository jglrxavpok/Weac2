package weac.lang

Pointer<Type> {

    private Type value;

    Type get() {
        if(value == null) {
            throw new NullPointerException();
        }
        return value;
    }

    void set(Type newValue) {
        this.value = newValue;
    }
}