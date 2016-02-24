package weac.compiler.precompile;

public class NewLocalToken extends Token {
    private final String type;
    private final String name;

    public NewLocalToken(String type, String name) {
        super(type+";"+name, TokenType.NEW_LOCAL, -1);
        this.type = type;
        this.name = name;
    }

    public String getLocalType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return super.toString()+" "+type+" "+name;
    }
}
