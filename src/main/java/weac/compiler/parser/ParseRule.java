package weac.compiler.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ParseRule {
    private final String trigger;
    private final List<ParseRule> subRules;
    private BiConsumer<Character, Parser> otherAction;
    private Consumer<Parser> action;

    public ParseRule(String trigger) {
        this.trigger = trigger;
        otherAction = (c, p) -> {};
        action = p -> {};
        subRules = new ArrayList<>();
    }

    public String getTrigger() {
        return trigger;
    }

    public ParseRule newSubRule(String string, Consumer<ParseRule> ruleInitializer) {
        ParseRule result = new ParseRule(string);
        ruleInitializer.accept(result);
        subRules.add(result);
        return result;
    }

    public void onOther(BiConsumer<Character, Parser> nextCharacter) {
        otherAction = nextCharacter;
    }

    public void discard() {
        // nothing
    }

    public void setAction(Consumer<Parser> runnable) {
        this.action = runnable;
    }

    public void apply(Parser parser) {
        action.accept(parser);
        Optional<ParseRule> subRule = subRules.stream()
                .filter(parser::applicable)
                .sorted((a, b) -> -Integer.compare(a.getTrigger().length(), b.getTrigger().length()))
                .findFirst();
        if(!subRule.isPresent()) {
            char character = '\0';
            if(!parser.isAtEnd()) {
                parser.mark();
                character = parser.nextCharacter();
                parser.rewind();
            }
            otherAction.accept(character, parser);
        } else {
            parser.forward(subRule.get().getTrigger().length());
            subRule.get().apply(parser);
        }
    }
}
