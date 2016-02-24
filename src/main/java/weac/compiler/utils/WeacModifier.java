package weac.compiler.utils;

public class WeacModifier {

    private final WeacModifierType type;

    public WeacModifier(WeacModifierType type) {
        this.type = type;
    }

    public WeacModifierType getType() {
        return type;
    }
}
