package weac.lang

import java.lang.String as JString

mixin class Number {

    String toString() {
        return JString.valueOf(this);
    }
}