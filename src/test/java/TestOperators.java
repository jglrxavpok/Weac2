import org.junit.Test;
import weac.compiler.CompileUtils;
import weac.compiler.utils.EnumOperators;

import static org.junit.Assert.assertEquals;

public class TestOperators {

    @Test
    public void readWithSpaceAtEnd() {
        assertEquals(EnumOperators.NOTEQUAL.raw(), CompileUtils.readOperator("!= ".toCharArray(), 0));
    }

    @Test
    public void readAmbiguous() {
        assertEquals(EnumOperators.INCREMENT.raw(), CompileUtils.readOperator("++".toCharArray(), 0));
    }

    @Test
    public void readDivideBy() {
        assertEquals(EnumOperators.DIVIDE_BY.raw(), CompileUtils.readOperator("/=".toCharArray(), 0));
    }
}
