package tests

import java.lang.Math as JMath
import weac.lang.Math
// Commented text 1

/*
 Commented text2
*/

class TestParse {
    #ifdef NotDefinedCondition
    Void nonCompiledField;
    #else
    Void compiledField;
    #end
}

public class PublicClass > MotherClass {
    Double myMethod(Float arg0, String arg1) {
        return arg0+arg1;
    }
}

protected class ProtectedClass > MyInterface + AnotherInterface {}

private class PrivateClass {}

struct MyStruct {
    Byte data;
}

object MyObject {
    Byte someConstantValue = 10b;
}
