package weac.compiler.resolve.structure;

import weac.compiler.precompile.structure.WeacPrecompiledField;
import weac.compiler.precompile.structure.WeacPrecompiledMethod;

import java.util.LinkedList;
import java.util.List;

public class WeacMixedContentClass {

    /**
     * The fields present in this class
     */
    public List<WeacPrecompiledField> fields;

    /**
     * The methods defined in this class
     */
    public List<WeacPrecompiledMethod> methods;

    public WeacMixedContentClass() {
        fields = new LinkedList<>();
        methods = new LinkedList<>();
    }

}
