import org.jglrxavpok.weac.precompile.WeacPreCompiler;
import org.jglrxavpok.weac.precompile.WeacToken;
import org.jglrxavpok.weac.precompile.WeacTokenType;
import org.jglrxavpok.weac.precompile.insn.*;
import org.jglrxavpok.weac.utils.EnumOperators;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TestPrecompile extends Tests {

    private WeacPreCompiler preCompiler;

    @Before
    public void init() {
        preCompiler = new WeacPreCompiler();
    }

    @Test
    public void testPrecompile() {
        precompile(preCompiler, "if(false) { 0.5f } else { -0.5f }", null);
    }

    @Test
    public void testComplexNestedMethods() {
        precompile(preCompiler, "myVar.myMethod(nestedMethod(\"My argument\", myVar2), \"MyString\".length(), constant.afield)",
                new WeacLoadVariable("myVar"), new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new WeacLoadStringConstant("My argument"), new WeacSimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR), new WeacLoadVariable("myVar2"),
                new WeacFunctionCall("nestedMethod", 2, false), new WeacSimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR),
                new WeacLoadStringConstant("MyString"), new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START), new WeacFunctionCall("length", 0, true),
                new WeacSimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR),
                new WeacLoadVariable("constant"), new WeacLoadVariable("afield"),
                new WeacFunctionCall("myMethod", 3, true));
    }

    @Test
    public void testSimpleNestedMethods() {
        precompile(preCompiler, "Math.sin(Math.random())",
                new WeacLoadVariable("Math"), new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new WeacLoadVariable("Math"), new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new WeacFunctionCall("random", 0, true),
                new WeacFunctionCall("sin", 1, true));
    }

    @Test
    public void testUnsignedRightShiftAssignment() {
        precompile(preCompiler, "myValue >>>= something", new WeacLoadVariable("myValue"), new WeacLoadVariable("something"), new WeacOperatorInsn(EnumOperators.APPLY_URSH));
    }

    @Test
    public void testDivideBy() {
        precompile(preCompiler, "myValue /= something", new WeacLoadVariable("myValue"), new WeacLoadVariable("something"), new WeacOperatorInsn(EnumOperators.DIVIDE_BY));
    }

    @Test
    public void testMultiplyBy() {
        precompile(preCompiler, "myValue *= something", new WeacLoadVariable("myValue"), new WeacLoadVariable("something"), new WeacOperatorInsn(EnumOperators.MULTIPLY_BY));
    }

    @Test
    public void testLeftShiftAssignment() {
        precompile(preCompiler, "myValue <<= something", new WeacLoadVariable("myValue"), new WeacLoadVariable("something"), new WeacOperatorInsn(EnumOperators.APPLY_LSH));
    }

    @Test
    public void testRightShiftAssignment() {
        precompile(preCompiler, "myValue >>= something", new WeacLoadVariable("myValue"), new WeacLoadVariable("something"), new WeacOperatorInsn(EnumOperators.APPLY_RSH));
    }

    @Test
    public void testLocalCreationAndSimpleAssignment() {
        precompile(preCompiler, "String myValue = something", new WeacNewLocalVar("String", "myValue"), new WeacLoadVariable("myValue"), new WeacLoadVariable("something"), new WeacOperatorInsn(EnumOperators.SET_TO));
    }

    @Test
    public void testWriteNumberConvertedToString() {
        precompile(preCompiler, "Console.writeLine((10).toString())",
                new WeacLoadVariable("Console"),
                new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new WeacLoadNumberConstant("10"),
                new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new WeacFunctionCall("toString", 0, true),
                new WeacFunctionCall("writeLine", 1, true));
    }

    @Test
    public void testReturnNumber() {
        precompile(preCompiler, "return 454", new WeacLoadNumberConstant("454"), new WeacSimplePreInsn(PrecompileOpcodes.RETURN));
    }

    @Test
    public void testFunctionCallWithArgument() {
        precompile(preCompiler, "myVar.myMethod(\"My argument\", myVar2)",
                new WeacLoadVariable("myVar"), new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new WeacLoadStringConstant("My argument"), new WeacSimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR),
                new WeacLoadVariable("myVar2"), new WeacFunctionCall("myMethod", 2, true));
    }

    @Test
    public void testFunctionCall() {
        precompile(preCompiler, "-global(01)", new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START), new WeacLoadNumberConstant("01"), new WeacFunctionCall("global", 1, false), new WeacOperatorInsn(EnumOperators.UNARY_MINUS));
    }

    @Test
    public void testReturnThis() {
        precompile(preCompiler, "return this", new WeacPrecompiledLoadThis(), new WeacSimplePreInsn(PrecompileOpcodes.RETURN));
    }

    @Test
    public void testReturnFalse() {
        precompile(preCompiler, "return false", new WeacLoadBooleanConstant(false), new WeacSimplePreInsn(PrecompileOpcodes.RETURN));
    }

    @Test
    public void testReturnAfterConstructorCall() {
        precompile(preCompiler, "return new Object", new WeacInstanciateInsn("Object"), new WeacFunctionCall("<init>", 0, true), new WeacSimplePreInsn(PrecompileOpcodes.RETURN));
    }

    @Test
    public void testUselessBrackets() {
        precompile(preCompiler, "(((0)))", new WeacLoadNumberConstant("0"));
    }

    @Test
    public void testIntervalCreation() {
        precompile(preCompiler, "(1..5).by(0.5f)",
                new WeacLoadNumberConstant("1"), new WeacLoadNumberConstant("5"), new WeacOperatorInsn(EnumOperators.INTERVAL_SEPARATOR),
                new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START), new WeacLoadNumberConstant("0.5f"), new WeacFunctionCall("by", 1, true));
    }

    @Test
    public void testArrayCreation() {
        precompile(preCompiler, "[1,2,5]",
                new WeacLoadNumberConstant("1"), new WeacSimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR), new WeacLoadNumberConstant("2"), new WeacSimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR), new WeacLoadNumberConstant("5"), new WeacCreateArray(3, "$$unknown"),
                new WeacSimplePreInsn(PrecompileOpcodes.DUP), new WeacStoreArray(2),
                new WeacSimplePreInsn(PrecompileOpcodes.DUP), new WeacStoreArray(1),
                new WeacSimplePreInsn(PrecompileOpcodes.DUP), new WeacStoreArray(0));
    }

    @Test
    public void testInstanceof() {
        precompile(preCompiler, "bar instanceof Foo", new WeacLoadVariable("bar"), new WeacLoadVariable("Foo"), new WeacOperatorInsn(EnumOperators.INSTANCEOF));
    }

    @Test
    public void testConstructorCallWithNoArgument() {
        precompile(preCompiler, "new Foo", new WeacInstanciateInsn("Foo"), new WeacFunctionCall("<init>", 0, true));
    }

    @Test
    public void testConstructorCallWithArgument() {
        precompile(preCompiler, "new Foo(bar)", new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START), new WeacLoadVariable("bar"), new WeacInstanciateInsn("Foo"), new WeacFunctionCall("<init>", 1, true));
    }

    @Test
    public void testSimpleConstructorCall() {
        precompile(preCompiler, "new Object()", new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START), new WeacInstanciateInsn("Object"), new WeacFunctionCall("<init>", 0, true));
    }

    @Test
    public void testInequality() {
        precompile(preCompiler, "100 != 0xAC", new WeacLoadNumberConstant("100"), new WeacLoadNumberConstant("0xAC"), new WeacOperatorInsn(EnumOperators.NOTEQUAL));
    }

    @Test
    public void testDivideNumbers() {
        precompile(preCompiler, "100/200", new WeacLoadNumberConstant("100"), new WeacLoadNumberConstant("200"), new WeacOperatorInsn(EnumOperators.DIVIDE));
    }

    @Test
    public void testMultiplyNumbers() {
        precompile(preCompiler, "100*200", new WeacLoadNumberConstant("100"), new WeacLoadNumberConstant("200"), new WeacOperatorInsn(EnumOperators.MULTIPLY));
    }

    @Test
    public void testSubtractNumbers() {
        precompile(preCompiler, "100-200", new WeacLoadNumberConstant("100"), new WeacLoadNumberConstant("200"), new WeacOperatorInsn(EnumOperators.MINUS));
    }

    @Test
    public void testAddNumbers() {
        precompile(preCompiler, "100+200", new WeacLoadNumberConstant("100"), new WeacLoadNumberConstant("200"), new WeacOperatorInsn(EnumOperators.PLUS));
    }

    @Test
    public void testAddChars() {
        precompile(preCompiler, "'0'+'2'", new WeacLoadCharacterConstant("0"), new WeacLoadCharacterConstant("2"), new WeacOperatorInsn(EnumOperators.PLUS));
    }

    @Test
    public void testLoadChar() {
        precompile(preCompiler, "'0'", new WeacLoadCharacterConstant("0"));
    }

    @Test
    public void testLoadString() {
        precompile(preCompiler, "\"A string\"", new WeacLoadStringConstant("A string"));
    }

    @Test
    public void testLoadByte() {
        precompile(preCompiler, "10b", new WeacLoadNumberConstant("10b"));
    }

    @Test
    public void testOperators() {
        precompile(preCompiler, "10*(-1)", new WeacLoadNumberConstant("10"), new WeacLoadNumberConstant("1"), new WeacOperatorInsn(EnumOperators.UNARY_MINUS), new WeacOperatorInsn(EnumOperators.MULTIPLY));
    }

    @Test
    public void testLoadHexadecimal() {
        precompile(preCompiler, "0x1000", new WeacLoadNumberConstant("0x1000"));
    }

    @Test
    public void testCustomBase() {
        precompile(preCompiler, "0c32#1000*0c3#101025", new WeacLoadNumberConstant("0c32#1000"), new WeacLoadNumberConstant("0c3#10102"), new WeacLoadNumberConstant("5"), new WeacOperatorInsn(EnumOperators.MULTIPLY)); // Custom base
    }

    @Test
    public void testLoadBinary() {
        precompile(preCompiler, "0b1010", new WeacLoadNumberConstant("0b1010"));
    }

    @Test
    public void testInstanceMethodCast() {
        precompile(preCompiler, "(Int) someInstance.toCast()", new WeacLoadVariable("someInstance"), new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START), new WeacFunctionCall("toCast", 0, true), new WeacCastPreInsn("Int"));
    }

    @Test
    public void testMethodCast() {
        precompile(preCompiler, "(Int) toCast()", new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START), new WeacFunctionCall("toCast", 0, false), new WeacCastPreInsn("Int"));
    }

    @Test
    public void testVariableCast() {
        precompile(preCompiler, "(Int)toCast", new WeacLoadVariable("toCast"), new WeacCastPreInsn("Int"));
    }

    @Test
    public void testCastedVariableMethodClass() {
        precompile(preCompiler, "((Int) toCast).toString()", new WeacLoadVariable("toCast"), new WeacCastPreInsn("Int"), new WeacSimplePreInsn(PrecompileOpcodes.FUNCTION_START), new WeacFunctionCall("toString", 0, true));
    }

    private void precompile(WeacPreCompiler preCompiler, String s) {

    }

    private void precompile(WeacPreCompiler preCompiler, String s, WeacPrecompiledInsn... expected) {
        System.out.println("[=== START OF PRECOMPILE OF \""+s+"\" ===]");
        List<WeacPrecompiledInsn> insns = preCompiler.precompileExpression(s, true);
        try {
            assertArrayEquals("Precompiled instructions and expected instructions do not match", expected, insns.toArray());
        } catch (AssertionError e) {
            System.err.println("Found: "+Arrays.toString(insns.toArray()));
            System.err.println("Expected: "+Arrays.toString(expected));
            throw e;
        }
        insns.forEach(System.out::println);
        System.out.println("[=== END ===]");
    }


}
