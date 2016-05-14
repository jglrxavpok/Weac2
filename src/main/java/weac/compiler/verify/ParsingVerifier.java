package weac.compiler.verify;

import weac.compiler.CompilePhase;
import weac.compiler.chop.EnumClassTypes;
import weac.compiler.chop.structure.ChoppedSource;
import weac.compiler.chop.structure.ChoppedClass;
import weac.compiler.chop.structure.ChoppedField;
import weac.compiler.chop.structure.ChoppedMethod;
import weac.compiler.utils.Identifier;

public class ParsingVerifier extends CompilePhase<ChoppedSource, ChoppedSource> {
    @Override
    public ChoppedSource process(ChoppedSource source) {
        // TODO: More verifications
        source.classes.forEach(this::verify);
        return source;
    }

    private void verify(ChoppedClass choppedClass) {
        verifyValidName(choppedClass.name.getCoreType().getIdentifier().getId(), choppedClass.startingLine);

        for (ChoppedField field : choppedClass.fields) {
            verifyValidName(field.name.getId(), field.startingLine);
        }

        for (ChoppedMethod method : choppedClass.methods) {
            verifyValidName(method.name.getId(), method.startingLine);
        }

        if(choppedClass.classType == EnumClassTypes.OBJECT) {
            choppedClass.methods.stream()
                    .filter(method -> method.isConstructor)
                    .filter(method -> !method.argumentNames.isEmpty())
                    .forEach(method -> newError(method.name+": constructors in object must not have arguments", -1));
        }

        if(choppedClass.classType == EnumClassTypes.DATA) {
            choppedClass.methods.stream()
                    .filter(method -> !method.isConstructor)
                    .forEach(method -> newError(method.name+": methods are not allowed in structs", -1));
        }

        if(choppedClass.isMixin) {
            if(!choppedClass.fields.isEmpty()) {
                newError("Mixins can not define fields", choppedClass.startingLine); // TODO: maybe add a workaround
            }
        }
    }

    private void verifyValidName(String name, int line) {
        if(!Identifier.isValid(name)) {
            newError("Invalid identifier start in name "+name, line);
        }
    }

    @Override
    public Class<ChoppedSource> getInputClass() {
        return ChoppedSource.class;
    }

    @Override
    public Class<ChoppedSource> getOutputClass() {
        return ChoppedSource.class;
    }
}
