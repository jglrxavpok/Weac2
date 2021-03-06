package tests

import java.lang.Math as JMath
import weac.lang.Math
// Commented text 1

/*
 Commented text2
*/

class TestParse {
    #ifdef NotDefinedCondition
    private Void nonCompiledField;
    #else
    private Void compiledField;
    #end
}

public class PublicClass > MotherClass {
    protected Double myMethod(Float arg0, String arg1) {
        return arg0+arg1;
    }
}

annotation TestAnnotation {

}

protected @TestAnnotation class ProtectedClass > MyInterface + AnotherInterface {}

private class PrivateClass {}

abstract class AbstractClass {
    abstract void methodA()
}

public enum MyEnum {
    VALUE0, VALUE1, VALUE2, VALUE3, VALUE4
}

public enum OtherEnum {
    VALUE0("value;"), VALUE1("value;"), VALUE2("value;"), VALUE3("value;"), VALUE4("value;");

    OtherEnum(String value) {
        ;;
    }
}

data MyData {
    Byte data;
    String[] otherStuff;
}

object MyObject {
    Byte someConstantValue = 10b;
}
