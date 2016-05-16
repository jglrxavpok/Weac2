package parser;

import org.junit.Test;
import weac.compiler.parser.ParseRule;
import weac.compiler.parser.Parser;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestParser {

    @Test(expected = NullPointerException.class)
    public void init() {
        new Parser(null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void endOfData() {
        Parser parser = new Parser("mydata");
        while(true)
            parser.nextCharacter();
    }

    @Test
    public void forward() {
        Parser parser = new Parser("markableData");
        String data = parser.forward(2);
        assertEquals("ma", data);
    }

    @Test
    public void backwards() {
        Parser parser = new Parser("markableData");
        parser.forward(4);
        String data = parser.backwards(2);
        assertEquals("rk", data);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void markAndRewind() {
        Parser parser = new Parser("markableData");
        parser.mark().forward(4);
        parser.rewind().backwards(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void pattern() {
        Parser parser = new Parser("+abc =48 +abc");
        parser.on("+", (rule) -> {
            rule.on("abc", ParseRule::discard);
            rule.onOther(c -> {
               throw new IllegalArgumentException("Invalid character following '+': "+c);
            });
        });

        parser.applyRules();
    }
}
