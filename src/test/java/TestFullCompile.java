import org.jglr.weac.WeacDefaultProcessor;
import org.junit.Test;

import java.io.IOException;

public class TestFullCompile extends Tests {

    @Test
    public void testCompile() throws IOException {
        WeacDefaultProcessor processor = new WeacDefaultProcessor();
        Object result = processor.process(read("tests/HelloWorld.ws"));
        System.out.println("Result is \n"+result);
    }
}
