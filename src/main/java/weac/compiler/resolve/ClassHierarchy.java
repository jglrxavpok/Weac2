package weac.compiler.resolve;

import weac.compiler.precompile.structure.PrecompiledClass;

import java.util.LinkedList;
import java.util.List;

public class ClassHierarchy {

    private PrecompiledClass superclass;
    private List<PrecompiledClass> interfaces;
    private List<PrecompiledClass> mixins;

    public ClassHierarchy() {
        interfaces = new LinkedList<>();
        mixins = new LinkedList<>();
        superclass = null;
    }

    public List<PrecompiledClass> getInterfaces() {
        return interfaces;
    }

    public List<PrecompiledClass> getMixins() {
        return mixins;
    }

    public void addMixin(PrecompiledClass mixin) {
        mixins.add(mixin);
    }

    public void addInterface(PrecompiledClass inter) {
        interfaces.add(inter);
    }

    public PrecompiledClass getSuperclass() {
        return superclass;
    }

    public void setSuperclass(PrecompiledClass superclass) {
        this.superclass = superclass;
    }

    public boolean hasInterface(String fullName) {
        return getInterfaces().stream().filter(i -> i.fullName.equals(fullName)).count() != 0;
    }
}
