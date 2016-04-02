package weac.compiler.verify;

import weac.compiler.CompilePhase;
import weac.compiler.parse.EnumClassTypes;
import weac.compiler.parse.structure.ParsedClass;
import weac.compiler.parse.structure.ParsedField;
import weac.compiler.parse.structure.ParsedMethod;
import weac.compiler.parse.structure.ParsedSource;
import weac.compiler.utils.Identifier;

public class ParsingVerifier extends CompilePhase<ParsedSource, ParsedSource> {
    @Override
    public ParsedSource process(ParsedSource source) {
        // TODO: More verifications
        source.classes.forEach(this::verify);
        return source;
    }

    private void verify(ParsedClass parsedClass) {
        verifyValidName(parsedClass.name.getCoreType().getIdentifier().getId(), parsedClass.startingLine);

        for (ParsedField field : parsedClass.fields) {
            verifyValidName(field.name.getId(), field.startingLine);
        }

        for (ParsedMethod method : parsedClass.methods) {
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
        if(!Identifier.isValid(name)) {
            newError("Invalid identifier start in name "+name, line);
        }
    }

    @Override
    public Class<ParsedSource> getInputClass() {
        return ParsedSource.class;
    }

    @Override
    public Class<ParsedSource> getOutputClass() {
        return ParsedSource.class;
    }
}
