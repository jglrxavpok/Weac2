package tests

object HelloWorld > Application + TestMixin {

    String[] myField;
    private Int fieldWithDefaultValue0 = 0
    private Int fieldWithDefaultValue1 = 0xA;
    private Int fieldWithDefaultValue2 = 0b1010;
    private Int fieldWithDefaultValue3 = 0o451;
    private Long fieldWithDefaultValue4 = 0c11#1L;
    private Int fieldWithDefaultValue5 = 0c4#10;
    private Int fieldWithDefaultValue6 = 0c98#10;
    private Float fieldWithDefaultValue7 = 78.545f;
    private Double fieldWithDefaultValue8 = 0.0D;
    private Double fieldWithDefaultValue9 = 10000000L;
    private Boolean fieldWithDefaultValue10 = false;
    private Boolean fieldWithDefaultValue11 = true;
    private String simpleField = "Test \n";


    Void main(String[] args) {
        Console.println("Hello World!");
    }

    String test() {
        return "TEST";
    }
}

mixin class TestMixin {
    void myMethodMixin() {
        // some code
        Console.println("Test");
    }
}