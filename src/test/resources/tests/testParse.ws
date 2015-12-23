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