package weac.compiler.utils;

import weac.compiler.parse.structure.ParsedAnnotation;

public class AnnotationModifier extends Modifier {

    private final ParsedAnnotation annotation;

    public AnnotationModifier(ModifierType type, ParsedAnnotation annotation) {
        super(type);
        this.annotation = annotation;
    }

    public ParsedAnnotation getAnnotation() {
        return annotation;
    }
}
