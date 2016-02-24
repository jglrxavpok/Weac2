package weac.compiler.resolve.structure;

import weac.compiler.precompile.structure.PrecompiledField;
import weac.compiler.precompile.structure.PrecompiledMethod;

import java.util.LinkedList;
import java.util.List;

public class MixedContentClass {

    /**
     * The fields present in this class
     */
    public List<PrecompiledField> fields;

    /**
     * The methods defined in this class
     */
    public List<PrecompiledMethod> methods;

    public MixedContentClass() {
        fields = new LinkedList<>();
        methods = new LinkedList<>();
    }

}
