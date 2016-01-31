package org.jglr.weac.verify;

import org.jglr.weac.WeacCompilePhase;
import org.jglr.weac.parse.EnumClassTypes;
import org.jglr.weac.parse.structure.WeacParsedClass;
import org.jglr.weac.parse.structure.WeacParsedField;
import org.jglr.weac.parse.structure.WeacParsedMethod;
import org.jglr.weac.parse.structure.WeacParsedSource;
import org.jglr.weac.utils.Identifier;

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

        if(parsedClass.classType == EnumClassTypes.OBJECT) {
            parsedClass.methods.stream()
                    .filter(method -> method.isConstructor)
                    .filter(method -> !method.argumentNames.isEmpty())
                    .forEach(method -> newError(method.name+": constructors in object must not have arguments", -1));
        }

        if(parsedClass.classType == EnumClassTypes.STRUCT) {
            parsedClass.methods.stream()
                    .filter(method -> !method.isConstructor)
                    .forEach(method -> newError(method.name+": methods are not allowed in structs", -1));
        }

        if(parsedClass.isMixin) {
            if(!parsedClass.fields.isEmpty()) {
                newError("Mixins can not define fields", parsedClass.startingLine); // TODO: maybe add a workaround
            }
        }
    }

    private void verifyValidName(String name, int line) {
        char[] chars = name.toCharArray();
        if(!Identifier.isIdentifierStart(chars[0])) {
            newError("Invalid identifier start in name "+name, line);
            return;
        }
        for(int i = 1;i<chars.length;i++) {
            if(!Identifier.isIdentifierPart(chars[i])) {
                newError("Invalid identifier part in name "+name, line);
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
