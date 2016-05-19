package weac.compiler.parser;

public class BlockDelimiter {
    private final String start;
    private final String end;
    private final boolean allowsSubBlocks;
    private boolean startDifferentFromEnd;
    private boolean closed;

    public BlockDelimiter(String start, String end, boolean allowSubBlocks) {
        closed = true;
        this.start = start;
        this.end = end;
        this.allowsSubBlocks = allowSubBlocks;
        startDifferentFromEnd = !start.equals(end);
    }

    public String getEnd() {
        return end;
    }

    public String getStart() {
        return start;
    }

    public boolean isStartDifferentFromEnd() {
        return startDifferentFromEnd;
    }

    public boolean allowsSubBloks() {
        return allowsSubBlocks;
    }

    public void switchState() {
        closed = !closed;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isApplicable(Parser parser, int index) {
        return parser.has(start, index) || parser.has(end, index);
    }

    @Override
    public String toString() {
        return "Delimiter["+start+" -> "+end+"]"+(allowsSubBlocks ? "" : ", no sub blocks");
    }
}
