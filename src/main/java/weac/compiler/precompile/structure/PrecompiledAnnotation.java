package weac.compiler.precompile.structure;

import weac.compiler.precompile.insn.PrecompiledInsn;

import java.util.LinkedList;
import java.util.List;

public class PrecompiledAnnotation {

    private final String name;
    public List<List<PrecompiledInsn>> args;

    public PrecompiledAnnotation(String name) {
        this.name = name;
        args = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public List<List<PrecompiledInsn>> getArgs() {
        return args;
    }
}
