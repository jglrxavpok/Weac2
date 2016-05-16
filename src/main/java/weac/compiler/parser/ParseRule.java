package weac.compiler.parser;

import java.util.function.Consumer;

public class ParseRule {
    public void on(String string, Consumer<ParseRule> ruleConsumer) {
        // TODO
    }

    public void onOther(Consumer<Character> nextCharacter) {
        // TODO
    }

    public void discard() {
        ; // nothing
    }
}
