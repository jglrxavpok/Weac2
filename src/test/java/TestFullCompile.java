import org.jglr.weac.WeacDefaultProcessor;
import org.jglr.weac.WeacMonolith;
import org.jglr.weac.parse.structure.WeacParsedSource;
import org.junit.Test;

import java.io.IOException;

public class TestFullCompile extends Tests {

    @Test
    public void testCompile() throws IOException {
        WeacMonolith monolith = new WeacMonolith();
        test(monolith, "tests/HelloWorld.ws");
        test(monolith, "weac/lang/Math.ws");
        test(monolith, "weac/lang/Application.ws");
    }

    private void test(WeacMonolith monolith, String s) throws IOException {
        monolith.compile(read(s));
    }
}
