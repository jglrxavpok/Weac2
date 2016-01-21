package org.jglr.weac.resolve.structure;

import org.jglr.weac.precompile.structure.WeacPrecompiledField;
import org.jglr.weac.precompile.structure.WeacPrecompiledMethod;

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
