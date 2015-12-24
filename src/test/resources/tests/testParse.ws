package tests

import java.lang.Math as JMath
import weac.lang.Math
// Commented text 1

/*
 Commented text2
*/

class TestParse {
    #ifdef NotDefinedCondition
    Void nonCompiledField
    #else
    Void compiledField
    #end
}

public class PublicClass > MotherClass{}

protected class ProtectedClass > MyInterface + AnotherInterface {}

private class PrivateClass {}