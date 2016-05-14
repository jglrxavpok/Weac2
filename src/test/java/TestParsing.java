import weac.compiler.PreProcessor;
import weac.compiler.chop.Chopper;
import weac.compiler.chop.structure.ChoppedSource;
import org.junit.Test;
import weac.compiler.utils.SourceCode;

import java.io.IOException;

import static org.junit.Assert.*;

public class TestParsing extends Tests {

    @Test
    public void testParser() throws IOException {
        PreProcessor preProcessor = new PreProcessor();
        Chopper chopper = new Chopper();
        ChoppedSource source = chopper.process(preProcessor.process(new SourceCode(read("tests/testParse.ws"))));
        System.out.println(source.sourceCode);
        assertFalse(source.sourceCode.contains("Commented text"));
        assertFalse(source.sourceCode.contains("Commented text2"));
        source.echo();
    }

    @Test
    public void testPreprocess() throws IOException {
        PreProcessor parser = new PreProcessor();
        String source = parser.process(new SourceCode(read("tests/testParse.ws"))).getContent();
        System.out.println(source);
        assertTrue(source.contains("compiledField"));
        assertFalse(source.contains("nonCompiledField"));
    }
}
