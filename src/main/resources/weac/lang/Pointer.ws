package weac.lang

Pointer<Type> {

    private Type value;

    Type get() {
        if(/*value == null*/false) {
            throw new NullPointerException();
        }
        return value;
    }

    void set(Type value) {
        this.value = value;
    }
}