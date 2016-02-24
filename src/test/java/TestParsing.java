import weac.compiler.WeacPreProcessor;
import weac.compiler.parse.structure.WeacParsedSource;
import weac.compiler.parse.WeacParser;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TestParsing extends Tests {

    @Test
    public void testParser() throws IOException {
        WeacPreProcessor preProcessor = new WeacPreProcessor();
        WeacParser parser = new WeacParser();
        WeacParsedSource source = parser.process(preProcessor.process(read("tests/testParse.ws")));
        System.out.println(source.sourceCode);
        assertFalse(source.sourceCode.contains("Commented text"));
        assertFalse(source.sourceCode.contains("Commented text2"));
        source.echo();
    }

    @Test
    public void testPreprocess() throws IOException {
        WeacPreProcessor parser = new WeacPreProcessor();
        String source = parser.process(read("tests/testParse.ws"));
        System.out.println(source);
        assertTrue(source.contains("compiledField"));
        assertFalse(source.contains("nonCompiledField"));
    }
}
