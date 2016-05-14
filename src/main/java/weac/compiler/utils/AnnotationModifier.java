package weac.compiler.utils;

import weac.compiler.chop.structure.ChoppedAnnotation;

public class AnnotationModifier extends Modifier {

    private final ChoppedAnnotation annotation;

    public AnnotationModifier(ModifierType type, ChoppedAnnotation annotation) {
        super(type);
        this.annotation = annotation;
    }

    public ChoppedAnnotation getAnnotation() {
        return annotation;
    }
}
