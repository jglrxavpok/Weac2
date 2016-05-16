package weac.compiler.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class Parser {

    private final String rawData;
    private final char[] characters;
    private final int length;
    private final Stack<Integer> markStack;
    private final List<ParseRule> rules;
    private int cursor;

    public Parser(String data) {
        if(data == null)
            throw new NullPointerException("'data' parameter cannot be null");
        this.rawData = data;
        this.characters = rawData.toCharArray();
        this.length = characters.length;
        markStack = new Stack<>();
        rules = new ArrayList<>();
    }

    public String getData() {
        return rawData;
    }

    public int getPosition() {
        return cursor;
    }

    public char nextCharacter() {
        if(cursor + 1 >= length) {
            throw new IndexOutOfBoundsException("Reached end of data");
        }
        return characters[cursor++];
    }

    public int getDataSize() {
        return length;
    }

    public String forward(int count) {
        if(count < 0)
            return backwards(-count);
        int oldCursor = cursor;
        cursor += count;
        if(cursor >= length) {
            throw new IndexOutOfBoundsException("Reached end of data");
        }
        return new String(characters, oldCursor, count);
    }

    public String backwards(int count) {
        if(count < 0)
            return forward(-count);
        int oldCursor = cursor;
        cursor -= count;
        if(cursor < 0) {
            throw new IndexOutOfBoundsException("Reached start of data, cannot go backwards");
        }
        return new String(characters, cursor, oldCursor-cursor);
    }

    public Parser mark() {
        markStack.push(cursor);
        return this;
    }

    public Parser rewind() {
        if(markStack.isEmpty())
            throw new IllegalStateException("Cannot rewind if data has not been marked yet");
        cursor = markStack.pop();
        return this;
    }

    public Parser seek(int position) {
        cursor = position;
        return this;
    }

    public Parser on(String string, Consumer<ParseRule> ruleConsumer) {
        ParseRule rule = newRule();
        rule.on(string, ruleConsumer);
        rules.add(rule);
        return this;
    }

    private ParseRule newRule() {
        return new ParseRule();
    }

    public void applyRules() {

    }
}
