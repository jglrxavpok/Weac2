package org.jglr.weac.utils;

public class AnnotationModifier extends WeacModifier {

    private final WeacAnnotation annotation;

    public AnnotationModifier(WeacModifierType type, WeacAnnotation annotation) {
        super(type);
        this.annotation = annotation;
    }
}
