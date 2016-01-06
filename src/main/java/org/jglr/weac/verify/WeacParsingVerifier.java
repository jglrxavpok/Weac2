package org.jglr.weac.verify;

import org.jglr.weac.WeacCompilePhase;
import org.jglr.weac.parse.structure.WeacParsedClass;
import org.jglr.weac.parse.structure.WeacParsedField;
import org.jglr.weac.parse.structure.WeacParsedMethod;
import org.jglr.weac.parse.structure.WeacParsedSource;

public class WeacParsingVerifier extends WeacCompilePhase<WeacParsedSource, WeacParsedSource> {
    @Override
    public WeacParsedSource process(WeacParsedSource source) {
        // TODO: More verifications
        source.classes.forEach(this::verify);
        return source;
    }

    private void verify(WeacParsedClass parsedClass) {
        verifyValidName(parsedClass.name, parsedClass.startingLine);

        for (WeacParsedField field : parsedClass.fields) {
            verifyValidName(field.name.getId(), field.startingLine);
        }

        for (WeacParsedMethod method : parsedClass.methods) {
            verifyValidName(method.name.getId(), method.startingLine);
        }
    }

    private void verifyValidName(String name, int line) {
        char[] chars = name.toCharArray();
        if(!Character.isJavaIdentifierStart(chars[0])) {
            newError("Invalid identifier start", line);
            return;
        }
        for(int i = 1;i<chars.length;i++) {
            if(!Character.isJavaIdentifierPart(chars[i])) {
                newError("Invalid identifier start", line);
                return;
            }
        }
    }

    @Override
    public Class<WeacParsedSource> getInputClass() {
        return WeacParsedSource.class;
    }

    @Override
    public Class<WeacParsedSource> getOutputClass() {
        return WeacParsedSource.class;
    }
}
