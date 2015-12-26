package weac.lang

import java.lang.Math as JMath

object Math {

    double sin(double arad) {
        return JMath.sin(arad);
    }

    double cos(double arad) {
        return JMath.cos(arad);
    }

    double tan(double arad) {
        return JMath.tan(arad);
    }

    double random() {
        return JMath.random();
    }
}