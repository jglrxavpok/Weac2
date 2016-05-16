package tests

import tests.TestValue
import java.util.function.Function

object HelloWorld > Application + TestMixin {

    String[] myField;
    private Int fieldWithDefaultValue0 = 0;
    private Int fieldWithDefaultValue1 = 0xA;
    private Int fieldWithDefaultValue2 = 0b1010;
    private Int fieldWithDefaultValue3 = 0o451;
    private Long fieldWithDefaultValue4 = 0c11#1L;
    private Int fieldWithDefaultValue5 = 0c4#10;
    private Int fieldWithDefaultValue6 = 0c98#10;
    private Float fieldWithDefaultValue7 = 78.545f;
    private Double fieldWithDefaultValue8 = 0.0D;
    private Double fieldWithDefaultValue9 = 10000000L;
    private var fieldWithDefaultValue10 = false;
    private var fieldWithDefaultValue11 = true;
    private var simpleField = "Test \n";


    Void start(String[] args) {
        Console.writeLine("Hello World!");
        this.myField = args;
        Console.writeLine(fieldWithDefaultValue0.factorial());
        #define TEST Math.sin(Math.random());

        #ifdef TEST
            TEST
        #end

        var v = new TestValue;
        v++;
        var v1 = v & v;
        v1(0);

        native {
            label l0
            getstatic weac/lang/Console __instance__ Lweac/lang/Console;
            ldc "Test"
            invokevirtual weac/lang/Console writeLine (Ljava/lang/String;)V
            aconst_null
            dup
            astore #v
            astore #v1
            goto l0
        }
        Console.writeLine(simpleField);
    }

    String test(String a) {
        return a;
    }

    Void testFunctionCall(Function f) {
        f("MyString!");
    }

    Object nativeTest() {
       native {
           aconst_null
           areturn
       }
    }

}

mixin class TestMixin {
    Void myMethodMixin() {
        // some code
        Console.writeLine("Test");
        Console.writeLine(([0..10]).toString());
    }
}

enum TestEnum {
    VALUE1("Test"), VALUE2("Test2");

    private String name;

    TestEnum(String _name) {
        this.name = _name;
    }
}

data MyData {
    Byte data;
    String[] otherStuff;
}