package weac.compiler.utils;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

public class IndentableWriter extends Writer {

    private final Writer delegate;
    private final String indentationText;
    private final StringBuffer indentationBuffer;
    private int indentationLevel;

    public IndentableWriter(Writer delegate) {
        this(delegate, "    ");
    }

    public IndentableWriter(Writer delegate, String indentation) {
        this.delegate = delegate;
        this.indentationText = indentation;
        indentationBuffer = new StringBuffer();
    }

    public void setIndentationLevel(int level) {
        if(level < 0) {
            throw new IllegalArgumentException("Indentation level can't be negative");
        }
        indentationLevel = level;
        int currentLevel = indentationBuffer.length() / indentationText.length();
        if(currentLevel > indentationLevel) {
            int charactersToRemove = (currentLevel- indentationLevel) * indentationText.length();
            indentationBuffer.delete(indentationBuffer.length()-charactersToRemove, indentationBuffer.length());
        } else if(currentLevel < indentationLevel) {
            for(int i = 0; i < indentationLevel - currentLevel; i++)
                indentationBuffer.append(indentationText);
        }
    }

    public int getIndentationLevel() {
        return indentationLevel;
    }

    public void incrementIndentation() {
        setIndentationLevel(indentationLevel+1);
    }

    public void decrementIndentation() {
        setIndentationLevel(indentationLevel-1);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        String start = new String(cbuf, off, len);
        String transformed = start.replace("\n", "\n"+indentationBuffer.toString());
        delegate.write(transformed.toCharArray(), 0, transformed.length());
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
