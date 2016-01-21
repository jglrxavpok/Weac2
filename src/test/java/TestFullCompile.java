import org.jglr.weac.WeacDefaultProcessor;
import org.jglr.weac.WeacMonolith;
import org.jglr.weac.parse.structure.WeacParsedSource;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TestFullCompile extends Tests {

    @Test
    public void testCompile() throws IOException, TimeoutException {
        WeacMonolith monolith = new WeacMonolith(new File("./src/main/resources/weac/lang/"));
        test(monolith, "tests/HelloWorld.ws");
        test(monolith, "weac/lang/Math.ws");
        test(monolith, "weac/lang/Application.ws");
    }

    private void test(WeacMonolith monolith, String s) throws IOException, TimeoutException {
        monolith.compile(read(s));
    }
}
