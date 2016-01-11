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
        return value % 1 == 0;
    }

}