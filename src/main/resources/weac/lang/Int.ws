package weac.lang

@PrimitiveLike("int")
class Int > Number {

    Int factorial() {
        return Math.fact(this);
    }
}