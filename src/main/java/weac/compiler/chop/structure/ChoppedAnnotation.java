package weac.compiler.chop.structure;

import weac.compiler.code.Member;
import weac.compiler.utils.ModifierType;
import weac.compiler.utils.WeacType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ChoppedAnnotation implements Member {

    private final String name;
    public List<String> args;
    public int startingLine;

    public ChoppedAnnotation(String name) {
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
    public ModifierType getAccess() {
        return ModifierType.PUBLIC;
    }

    @Override
    public List<WeacType> getGenericParameterNames() {
        return Collections.emptyList();
    }

    public List<String> getArgs() {
        return args;
    }
}
