package org.jglr.weac.resolve;

import org.jglr.weac.precompile.structure.WeacPrecompiledClass;
import org.jglr.weac.resolve.structure.WeacResolvedClass;

import java.util.LinkedList;
import java.util.List;

public class ClassParents {

    private WeacPrecompiledClass superclass;
    private List<WeacPrecompiledClass> interfaces;
    private List<WeacPrecompiledClass> mixins;

    public ClassParents() {
        interfaces = new LinkedList<>();
        mixins = new LinkedList<>();
        superclass = null;
    }

    public List<WeacPrecompiledClass> getInterfaces() {
        return interfaces;
    }

    public List<WeacPrecompiledClass> getMixins() {
        return mixins;
    }

    public void addMixin(WeacPrecompiledClass mixin) {
        mixins.add(mixin);
    }

    public void addInterface(WeacPrecompiledClass inter) {
        interfaces.add(inter);
    }

    public WeacPrecompiledClass getSuperclass() {
        return superclass;
    }

    public void setSuperclass(WeacPrecompiledClass superclass) {
        this.superclass = superclass;
    }
}
