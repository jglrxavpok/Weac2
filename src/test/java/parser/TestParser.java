package parser;

import org.junit.Test;
import weac.compiler.parser.ParseRule;
import weac.compiler.parser.Parser;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void backwardsTo() {
        Parser parser = new Parser("markableData");
        parser.forwardTo("Data");
        String data = parser.backwardsTo("a");
        assertEquals("ble", data);
    }

    @Test
    public void forwardTo() {
        Parser parser = new Parser("markableData");
        String data = parser.forwardTo("a");
        assertEquals("m", data);
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
        parser.mark()
                .forward(4);
        parser.rewind()
                .backwards(1); // raises an exception
    }

    @Test
    public void readBlock() {
        StringBuffer buffer = new StringBuffer();
        Parser parser = new Parser("; Something that we don't care about\n" +
                "import java.util.{Collection, List} as Test");
        parser.addBlockDelimiters("{", "}", true);
        parser.newRule(";", r -> {
            System.out.println("read: "+parser.forwardToOrEnd("\n"));
            parser.mark();
            System.out.println("next: "+parser.forward(8));
            parser.rewind();
        });
        parser.newRule("\r", ParseRule::discard);
        parser.newRule("\n", ParseRule::discard);
        parser.newRule(" ", ParseRule::discard);
        parser.newRule("as", r -> {
            r.setAction(() -> {
                parser.forwardUntilNot(" ");
                String name = parser.forwardToOrEnd("\n");
                buffer.append("=").append(name);
            });
        });
        parser.newRule("import", r -> {
            r.setAction(() -> {
                parser.enableBlocks();
                parser.forwardUntilNot(" ");
                String result = parser.forwardToOrEnd(" ");
                buffer.append(result);
                parser.disableBlocks();
            });
        });
        parser.applyRules();
        assertEquals("java.util.{Collection, List}=Test", buffer.toString());
    }

    @Test
    public void pattern() {
        Parser parser = new Parser("+abc =48 +14");
        parser.newRule("+", rule -> {
            rule.setAction(() -> {
                System.out.println("Found '+'");
            });
            rule.newSubRule("abc", subRule -> {
                System.out.println("yay abc");
            });
            rule.onOther((c, p) -> {
                System.out.println("Could not find correct remaining, found: "+p.forwardToOrEnd(" "));
            });
        });
        parser.newRule(" ", ParseRule::discard);
        AtomicInteger counter = new AtomicInteger();
        parser.newRule("=", r -> {
            r.setAction(() -> {
                System.out.println("yay2");
            });
            r.onOther((c, p) -> {
                String numberString = p.forwardToOrEnd(" ");
                if(!numberString.isEmpty()) {
                    int number = Integer.parseInt(numberString);
                    counter.addAndGet(number);
                }
            });
        });
        parser.applyRules();
        assertEquals(48, counter.get());
    }
}
