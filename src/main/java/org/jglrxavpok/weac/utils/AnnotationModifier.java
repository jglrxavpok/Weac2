package org.jglrxavpok.weac.utils;

import org.jglrxavpok.weac.parse.structure.WeacParsedAnnotation;

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
