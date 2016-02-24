package weac.compiler.utils;

import weac.compiler.parse.structure.WeacParsedAnnotation;

public class AnnotationModifier extends WeacModifier {

    private final WeacParsedAnnotation annotation;

    public AnnotationModifier(WeacModifierType type, WeacParsedAnnotation annotation) {
        super(type);
        this.annotation = annotation;
    }

    public WeacParsedAnnotation getAnnotation() {
        return annotation;
    }
}
