import org.jglr.weac.precompile.WeacPreCompiler;
import org.jglr.weac.precompile.insn.Opcodes;
import org.jglr.weac.precompile.insn.WeacPrecompiledInsn;
import org.junit.Test;

import java.util.List;

public class TestPrecompile extends Tests {

    @Test
    public void testPrecompile() {
        WeacPreCompiler preCompiler = new WeacPreCompiler();
        precompile(preCompiler, "10b");
        precompile(preCompiler, "10*(-1)");
        precompile(preCompiler, "0x1000");
        precompile(preCompiler, "0c32#1000*0c3#101025"); // Custom base
        precompile(preCompiler, "0b1010");
        precompile(preCompiler, "\"A string\"");
        precompile(preCompiler, "'0'");
        precompile(preCompiler, "'0'+'2'");
        precompile(preCompiler, "100+200");
        precompile(preCompiler, "100-200");
        precompile(preCompiler, "100^200");
        precompile(preCompiler, "100*200");
        precompile(preCompiler, "100 != 0xAC");
        precompile(preCompiler, "new Object()");
        precompile(preCompiler, "new Foo(bar)");
        precompile(preCompiler, "bar instanceof Foo");
        precompile(preCompiler, "[1,2,5]");
        precompile(preCompiler, "[1..5].by(0.5f)");
        precompile(preCompiler, "global(01)");
        precompile(preCompiler, "myVar.myMethod(\"My argument\", myVar2)");
        precompile(preCompiler, "myVar.myMethod(nestedMethod(\"My argument\", myVar2), \"MyString\".length(), constant.afield)");
    }

    private void precompile(WeacPreCompiler preCompiler, String s) {
        System.out.println("[=== START OF PRECOMPILE OF \""+s+"\" ===]");
        List<WeacPrecompiledInsn> insns = preCompiler.precompileExpression(s);
        insns.forEach(System.out::println);
        System.out.println("[=== END ===]");
    }
}
