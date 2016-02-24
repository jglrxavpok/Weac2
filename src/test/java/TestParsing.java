import weac.compiler.PreProcessor;
import weac.compiler.parse.Parser;
import weac.compiler.parse.structure.ParsedSource;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TestParsing extends Tests {

    @Test
    public void testParser() throws IOException {
        PreProcessor preProcessor = new PreProcessor();
        Parser parser = new Parser();
        ParsedSource source = parser.process(preProcessor.process(read("tests/testParse.ws")));
        System.out.println(source.sourceCode);
        assertFalse(source.sourceCode.contains("Commented text"));
        assertFalse(source.sourceCode.contains("Commented text2"));
        source.echo();
    }

    @Test
    public void testPreprocess() throws IOException {
        PreProcessor parser = new PreProcessor();
        String source = parser.process(read("tests/testParse.ws"));
        System.out.println(source);
        assertTrue(source.contains("compiledField"));
        assertFalse(source.contains("nonCompiledField"));
    }
}
