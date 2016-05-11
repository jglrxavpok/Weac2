package tests

TestValue {

    TestValue unary++() {
        // Do something
        return this;
    }

    TestValue operator&(TestValue other) {
        return other;
    }

    Int call(Int a) {
       return a;
    }

}
