import org.jglr.weac.WeacDefaultProcessor;
import org.jglr.weac.parse.structure.WeacParsedSource;
import org.junit.Test;

import java.io.IOException;

public class TestFullCompile extends Tests {

    @Test
    public void testCompile() throws IOException {
        WeacDefaultProcessor processor = new WeacDefaultProcessor();
        test(processor, "tests/HelloWorld.ws");
        test(processor, "weac/lang/Math.ws");
        test(processor, "weac/lang/Application.ws");
    }

    private void test(WeacDefaultProcessor processor, String s) throws IOException {
        Object result = processor.process(read(s));
        System.out.println("Result is \n"+result);
        if(result instanceof WeacParsedSource) {
            ((WeacParsedSource) result).echo();
        }
    }
}
