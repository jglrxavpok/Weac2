import weac.compiler.precompile.PreCompiler;
import weac.compiler.precompile.insn.*;
import weac.compiler.utils.EnumOperators;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TestPrecompile extends Tests {

    private PreCompiler preCompiler;

    @Before
    public void init() {
        preCompiler = new PreCompiler();
    }

    @Test
    public void testPrecompile() {
        precompile(preCompiler, "if(false) { 0.5f } else { -0.5f }", null);
    }

    @Test
    public void testComplexNestedMethods() {
        precompile(preCompiler, "myVar.myMethod(nestedMethod(\"My argument\", myVar2), \"MyString\".length(), constant.afield)",
                new LoadVariable("myVar"), new SimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new SimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new LoadStringConstant("My argument"), new SimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR), new LoadVariable("myVar2"),
                new FunctionCall("nestedMethod", 2, false), new SimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR),
                new LoadStringConstant("MyString"), new SimplePreInsn(PrecompileOpcodes.FUNCTION_START), new FunctionCall("length", 0, true),
                new SimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR),
                new LoadVariable("constant"), new LoadVariable("afield"),
                new FunctionCall("myMethod", 3, true));
    }

    @Test
    public void testSimpleNestedMethods() {
        precompile(preCompiler, "Math.sin(Math.random())",
                new LoadVariable("Math"), new SimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new LoadVariable("Math"), new SimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new FunctionCall("random", 0, true),
                new FunctionCall("sin", 1, true));
    }

    @Test
    public void testUnsignedRightShiftAssignment() {
        precompile(preCompiler, "myValue >>>= something", new LoadVariable("myValue"), new LoadVariable("something"), new OperatorInsn(EnumOperators.APPLY_URSH));
    }

    @Test
    public void testDivideBy() {
        precompile(preCompiler, "myValue /= something", new LoadVariable("myValue"), new LoadVariable("something"), new OperatorInsn(EnumOperators.DIVIDE_BY));
    }

    @Test
    public void testMultiplyBy() {
        precompile(preCompiler, "myValue *= something", new LoadVariable("myValue"), new LoadVariable("something"), new OperatorInsn(EnumOperators.MULTIPLY_BY));
    }

    @Test
    public void testLeftShiftAssignment() {
        precompile(preCompiler, "myValue <<= something", new LoadVariable("myValue"), new LoadVariable("something"), new OperatorInsn(EnumOperators.APPLY_LSH));
    }

    @Test
    public void testRightShiftAssignment() {
        precompile(preCompiler, "myValue >>= something", new LoadVariable("myValue"), new LoadVariable("something"), new OperatorInsn(EnumOperators.APPLY_RSH));
    }

    @Test
    public void testLocalCreationAndSimpleAssignment() {
        precompile(preCompiler, "String myValue = something", new NewLocalVar("String", "myValue"), new LoadVariable("myValue"), new LoadVariable("something"), new OperatorInsn(EnumOperators.SET_TO));
    }

    @Test
    public void testWriteNumberConvertedToString() {
        precompile(preCompiler, "Console.writeLine((10).toString())",
                new LoadVariable("Console"),
                new SimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new LoadNumberConstant("10"),
                new SimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new FunctionCall("toString", 0, true),
                new FunctionCall("writeLine", 1, true));
    }

    @Test
    public void testReturnNumber() {
        precompile(preCompiler, "return 454", new LoadNumberConstant("454"), new SimplePreInsn(PrecompileOpcodes.RETURN));
    }

    @Test
    public void testFunctionCallWithArgument() {
        precompile(preCompiler, "myVar.myMethod(\"My argument\", myVar2)",
                new LoadVariable("myVar"), new SimplePreInsn(PrecompileOpcodes.FUNCTION_START),
                new LoadStringConstant("My argument"), new SimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR),
                new LoadVariable("myVar2"), new FunctionCall("myMethod", 2, true));
    }

    @Test
    public void testFunctionCall() {
        precompile(preCompiler, "-global(01)", new SimplePreInsn(PrecompileOpcodes.FUNCTION_START), new LoadNumberConstant("01"), new FunctionCall("global", 1, false), new OperatorInsn(EnumOperators.UNARY_MINUS));
    }

    @Test
    public void testReturnThis() {
        precompile(preCompiler, "return this", new PrecompiledLoadThis(), new SimplePreInsn(PrecompileOpcodes.RETURN));
    }

    @Test
    public void testReturnFalse() {
        precompile(preCompiler, "return false", new LoadBooleanConstant(false), new SimplePreInsn(PrecompileOpcodes.RETURN));
    }

    @Test
    public void testReturnAfterConstructorCall() {
        precompile(preCompiler, "return new Object", new InstanciateInsn("Object"), new FunctionCall("<init>", 0, true), new SimplePreInsn(PrecompileOpcodes.RETURN));
    }

    @Test
    public void testUselessBrackets() {
        precompile(preCompiler, "(((0)))", new LoadNumberConstant("0"));
    }

    @Test
    public void testIntervalCreation() {
        precompile(preCompiler, "(1..5).by(0.5f)",
                new LoadNumberConstant("1"), new LoadNumberConstant("5"), new OperatorInsn(EnumOperators.INTERVAL_SEPARATOR),
                new SimplePreInsn(PrecompileOpcodes.FUNCTION_START), new LoadNumberConstant("0.5f"), new FunctionCall("by", 1, true));
    }

    @Test
    public void testArrayCreation() {
        precompile(preCompiler, "[1,2,5]",
                new LoadNumberConstant("1"), new SimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR), new LoadNumberConstant("2"), new SimplePreInsn(PrecompileOpcodes.ARGUMENT_SEPARATOR), new LoadNumberConstant("5"), new CreateArray(3, "$$unknown"),
                new SimplePreInsn(PrecompileOpcodes.DUP), new StoreArray(2),
                new SimplePreInsn(PrecompileOpcodes.DUP), new StoreArray(1),
                new SimplePreInsn(PrecompileOpcodes.DUP), new StoreArray(0));
    }

    @Test
    public void testInstanceof() {
        precompile(preCompiler, "bar instanceof Foo", new LoadVariable("bar"), new LoadVariable("Foo"), new OperatorInsn(EnumOperators.INSTANCEOF));
    }

    @Test
    public void testConstructorCallWithNoArgument() {
        precompile(preCompiler, "new Foo", new InstanciateInsn("Foo"), new FunctionCall("<init>", 0, true));
    }

    @Test
    public void testConstructorCallWithArgument() {
        precompile(preCompiler, "new Foo(bar)", new SimplePreInsn(PrecompileOpcodes.FUNCTION_START), new LoadVariable("bar"), new InstanciateInsn("Foo"), new FunctionCall("<init>", 1, true));
    }

    @Test
    public void testSimpleConstructorCall() {
        precompile(preCompiler, "new Object()", new SimplePreInsn(PrecompileOpcodes.FUNCTION_START), new InstanciateInsn("Object"), new FunctionCall("<init>", 0, true));
    }

    @Test
    public void testInequality() {
        precompile(preCompiler, "100 != 0xAC", new LoadNumberConstant("100"), new LoadNumberConstant("0xAC"), new OperatorInsn(EnumOperators.NOTEQUAL));
    }

    @Test
    public void testDivideNumbers() {
        precompile(preCompiler, "100/200", new LoadNumberConstant("100"), new LoadNumberConstant("200"), new OperatorInsn(EnumOperators.DIVIDE));
    }

    @Test
    public void testMultiplyNumbers() {
        precompile(preCompiler, "100*200", new LoadNumberConstant("100"), new LoadNumberConstant("200"), new OperatorInsn(EnumOperators.MULTIPLY));
    }

    @Test
    public void testSubtractNumbers() {
        precompile(preCompiler, "100-200", new LoadNumberConstant("100"), new LoadNumberConstant("200"), new OperatorInsn(EnumOperators.MINUS));
    }

    @Test
    public void testAddNumbers() {
        precompile(preCompiler, "100+200", new LoadNumberConstant("100"), new LoadNumberConstant("200"), new OperatorInsn(EnumOperators.PLUS));
    }

    @Test
    public void testAddChars() {
        precompile(preCompiler, "'0'+'2'", new LoadCharacterConstant("0"), new LoadCharacterConstant("2"), new OperatorInsn(EnumOperators.PLUS));
    }

    @Test
    public void testLoadChar() {
        precompile(preCompiler, "'0'", new LoadCharacterConstant("0"));
    }

    @Test
    public void testLoadString() {
        precompile(preCompiler, "\"A string\"", new LoadStringConstant("A string"));
    }

    @Test
    public void testLoadByte() {
        precompile(preCompiler, "10b", new LoadNumberConstant("10b"));
    }

    @Test
    public void testOperators() {
        precompile(preCompiler, "10*(-1)", new LoadNumberConstant("10"), new LoadNumberConstant("1"), new OperatorInsn(EnumOperators.UNARY_MINUS), new OperatorInsn(EnumOperators.MULTIPLY));
    }

    @Test
    public void testLoadHexadecimal() {
        precompile(preCompiler, "0x1000", new LoadNumberConstant("0x1000"));
    }

    @Test
    public void testCustomBase() {
        precompile(preCompiler, "0c32#1000*0c3#101025", new LoadNumberConstant("0c32#1000"), new LoadNumberConstant("0c3#10102"), new LoadNumberConstant("5"), new OperatorInsn(EnumOperators.MULTIPLY)); // Custom base
    }

    @Test
    public void testLoadBinary() {
        precompile(preCompiler, "0b1010", new LoadNumberConstant("0b1010"));
    }

    @Test
    public void testInstanceMethodCast() {
        precompile(preCompiler, "(Int) someInstance.toCast()", new LoadVariable("someInstance"), new SimplePreInsn(PrecompileOpcodes.FUNCTION_START), new FunctionCall("toCast", 0, true), new CastPreInsn("Int"));
    }

    @Test
    public void testMethodCast() {
        precompile(preCompiler, "(Int) toCast()", new SimplePreInsn(PrecompileOpcodes.FUNCTION_START), new FunctionCall("toCast", 0, false), new CastPreInsn("Int"));
    }

    @Test
    public void testVariableCast() {
        precompile(preCompiler, "(Int)toCast", new LoadVariable("toCast"), new CastPreInsn("Int"));
    }

    @Test
    public void testCastedVariableMethodClass() {
        precompile(preCompiler, "((Int) toCast).toString()", new LoadVariable("toCast"), new CastPreInsn("Int"), new SimplePreInsn(PrecompileOpcodes.FUNCTION_START), new FunctionCall("toString", 0, true));
    }

    private void precompile(PreCompiler preCompiler, String s) {

    }

    private void precompile(PreCompiler preCompiler, String s, PrecompiledInsn... expected) {
        System.out.println("[=== START OF PRECOMPILE OF \""+s+"\" ===]");
        List<PrecompiledInsn> insns = preCompiler.precompileExpression(s, true);
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
