import org.jglr.weac.WeacPreProcessor;
import org.jglr.weac.parse.structure.WeacParsedSource;
import org.jglr.weac.parse.WeacParser;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TestParsing extends Tests {

    @Test
    public void testParser() throws IOException {
        WeacPreProcessor preProcessor = new WeacPreProcessor();
        WeacParser parser = new WeacParser();
        WeacParsedSource source = parser.parseSource(preProcessor.preprocess(read("tests/testParse.ws")));
        System.out.println(source.sourceCode);
        assertFalse(source.sourceCode.contains("Commented text"));
        assertFalse(source.sourceCode.contains("Commented text2"));
        source.echo();
    }

    @Test
    public void testPreprocess() throws IOException {
        WeacPreProcessor parser = new WeacPreProcessor();
        String source = parser.preprocess(read("tests/testParse.ws"));
        System.out.println(source);
        assertTrue(source.contains("compiledField"));
        assertFalse(source.contains("nonCompiledField"));
    }
}
