package weac.compiler.resolve;

import weac.compiler.precompile.structure.WeacPrecompiledClass;

import java.util.LinkedList;
import java.util.List;

public class ClassHierarchy {

    private WeacPrecompiledClass superclass;
    private List<WeacPrecompiledClass> interfaces;
    private List<WeacPrecompiledClass> mixins;

    public ClassHierarchy() {
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

    public boolean hasInterface(String fullName) {
        return getInterfaces().stream().filter(i -> i.fullName.equals(fullName)).count() != 0;
    }
}
