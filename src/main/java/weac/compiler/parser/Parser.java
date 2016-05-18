package weac.compiler.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

public class Parser {

    private final String rawData;
    private final char[] characters;
    private final int length;
    private final Stack<Integer> markStack;
    private final List<ParseRule> rules;
    private final List<BlockDelimiter> delimiters;
    private int cursor;
    private boolean blocksEnabled;

    public Parser(String data) {
        if(data == null)
            throw new NullPointerException("'data' parameter cannot be null");
        this.rawData = data;
        this.characters = rawData.toCharArray();
        this.length = characters.length;
        markStack = new Stack<>();
        rules = new ArrayList<>();
        delimiters = new ArrayList<>();
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
        if(cursor > length) {
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

    public void applyRules() {
        while(true) {
            Optional<ParseRule> rule = rules.stream()
                    .filter(this::applicable)
                    .sorted((a, b) -> -Integer.compare(a.getTrigger().length(), b.getTrigger().length()))
                    .findFirst();
            if(!rule.isPresent()) {
                break;
            }
            forward(rule.get().getTrigger().length());
            rule.get().apply(this);
        }
    }

    public boolean applicable(ParseRule rule) {
        return rawData.indexOf(rule.getTrigger(), cursor) == cursor;
    }

    public boolean hasNextCharacter() {
        return cursor < length;
    }

    /**
     * Reads the data from the current position up to the given destination, stopping right before it and going backwards
     * @param destination
     *      The string to stop at
     * @return
     *      The read string
     * @throws StringIndexOutOfBoundsException If destination could not be find in the data string
     */
    public String backwardsTo(String destination) {
        if(backwardsIndexOf(rawData, destination, cursor) < 0)
            throw new StringIndexOutOfBoundsException("Could not find '"+destination+"' to forward to");
        int dest = backwardsIndexOf(rawData, destination, cursor) +1;
        return backwards(cursor-dest);
    }

    /**
     * Same as {@link String#indexOf(String)} but goes from end to start of string instead of start to end
     * @param hay
     * @param needle
     * @param start
     * @return
     */
    private int backwardsIndexOf(String hay, String needle, int start) {
        for (int i = start; i >= 0; i--) {
            if(hay.indexOf(needle, i) == i)
                return i;
        }
        return -1;
    }

    /**
     * Reads the data from the current position up to the given destination, stopping right before it
     * @param destination
     *      The string to stop at
     * @return
     *      The read string
     * @throws StringIndexOutOfBoundsException If destination could not be find in the data string
     */
    public String forwardTo(String destination) {
        if(rawData.indexOf(destination, cursor) < 0)
            throw new StringIndexOutOfBoundsException("Could not find '"+destination+"' to forward to");
        if(!blocksEnabled) {
            int dest = rawData.indexOf(destination, cursor);
            return forward(dest - cursor);
        } else {
            Stack<BlockDelimiter> blockStack = new Stack<>();
            BlockDelimiter currentDelimiter = null;
            for (int i = cursor; i < length; i++) {
                int tempIndex = i;
                Optional<BlockDelimiter> optional = delimiters.stream()
                        .filter(d -> d.isApplicable(this, tempIndex))
                        .findFirst();
                if(optional.isPresent()) {
                    BlockDelimiter delimiter = optional.get();
                    if(currentDelimiter != null && delimiter != currentDelimiter) {
                        if(!currentDelimiter.allowsSubBloks()) {
                            continue;
                        }
                    }
                    if(delimiter.isStartDifferentFromEnd()) {
                        if(has(delimiter.getStart(), i)) {
                            currentDelimiter = delimiter;
                            blockStack.push(delimiter);
                        } else {
                            currentDelimiter = blockStack.pop();
                        }
                    } else {
                        delimiter.switchState();
                        if(delimiter.isClosed()) {
                            currentDelimiter = blockStack.pop();
                        } else {
                            currentDelimiter = delimiter;
                            blockStack.push(delimiter);
                        }
                    }
                }

                if(rawData.indexOf(destination, i) == i && blockStack.isEmpty())
                    return forward(i - cursor);
            }
            throw new StringIndexOutOfBoundsException("Could not find '"+destination+"' to forward to (blocks are enabled)");
        }
    }

    public boolean isAt(String string) {
        return has(string, cursor);
    }

    public String forwardToOrEnd(String destination) {
        mark();
        try {
            String result = forwardTo(destination);
            discardMark();
            return result;
        } catch (StringIndexOutOfBoundsException e) {
            rewind();
            return forward(length-cursor);
        }
    }

    private void discardMark() {
        markStack.pop();
    }

    public ParseRule newRule(String trigger) {
        return newRule(trigger, r -> {});
    }

    public ParseRule newRule(String trigger, Consumer<ParseRule> ruleInitializer) {
        ParseRule rule = new ParseRule(trigger);
        ruleInitializer.accept(rule);
        rules.add(rule);
        return rule;
    }

    public String forwardUntilNot(String toAvoid) {
        int dest = cursor;
        while(rawData.indexOf(toAvoid, dest) == dest && dest < length) {
            dest++;
        }
        return forward(dest-cursor);
    }

    public Parser addBlockDelimiters(String start, String end, boolean allowSubBlocks) {
        delimiters.add(new BlockDelimiter(start, end, allowSubBlocks));
        return this;
    }

    public Parser enableBlocks() {
        blocksEnabled = true;
        return this;
    }

    public Parser disableBlocks() {
        blocksEnabled = false;
        return this;
    }

    public boolean has(String string, int at) {
        return rawData.indexOf(string, at) == at;
    }

    public boolean isAtEnd() {
        return length <= cursor;
    }
}
