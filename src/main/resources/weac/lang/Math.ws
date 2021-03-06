package weac.lang

import java.lang.Math as JMath

object Math {

    Double sin(Double arad) {
        return JMath.sin(arad);
    }

    Double cos(Double arad) {
        return JMath.cos(arad);
    }

    Double tan(Double arad) {
        return JMath.tan(arad);
    }

    Double random() {
        return JMath.random();
    }

    Boolean isInteger(Double value) {
        return (value % 1) == 0;
    }

    Double abs(Double value) {
        return JMath.abs(value);
    }

    Int fact(Int number) {
        return fact_tail(1, number);
    }

    private Int fact_tail(Int acc, Int n) {
        if(n < 2) {
            return acc;
        }
        return fact_tail(n * acc, n-1);
    }

}