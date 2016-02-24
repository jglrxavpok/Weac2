package weac.compiler.parse.structure;

import weac.compiler.code.WeacMember;
import weac.compiler.utils.WeacModifierType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class WeacParsedAnnotation implements WeacMember {

    private final String name;
    public List<String> args;

    public WeacParsedAnnotation(String name) {
        this.name = name;
        args = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    @Override
    public String getCanonicalName() {
        return getName();
    }

    @Override
    public WeacModifierType getAccess() {
        return WeacModifierType.PUBLIC;
    }

    @Override
    public List<String> getGenericParameterNames() {
        return Collections.emptyList();
    }

    public List<String> getArgs() {
        return args;
    }
}
