package weac.lang

@PrimitiveLike("int")
Int > Number {

    Int factorial() {
        return Math.fact(this);
    }
}