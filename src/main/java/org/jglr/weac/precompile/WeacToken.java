package org.jglr.weac.precompile;

public class WeacToken {

    private final String content;
    private WeacTokenType type;
    public final int length;

    public WeacToken(String content, WeacTokenType type, int length) {
        this.content = content;
        this.length = length;
        this.type = type;
    }

    public WeacTokenType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public void setType(WeacTokenType type) {
        this.type = type;
    }
}
