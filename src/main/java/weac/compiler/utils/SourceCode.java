package weac.compiler.utils;

import java.util.UUID;

public class SourceCode {

    private final String fileName;
    private final String content;

    public SourceCode(String content) {
        this("Non-file source ("+ UUID.randomUUID()+")", content);
    }

    public SourceCode(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getFileName() {
        return fileName;
    }
}
