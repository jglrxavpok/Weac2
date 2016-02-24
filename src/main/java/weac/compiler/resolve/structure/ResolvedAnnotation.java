package weac.compiler.resolve.structure;

import weac.compiler.resolve.insn.ResolvedInsn;

import java.util.LinkedList;
import java.util.List;

public class ResolvedAnnotation {

    private final ResolvedClass name;
    public List<List<ResolvedInsn>> args;

    public ResolvedAnnotation(ResolvedClass type) {
        this.name = type;
        args = new LinkedList<>();
    }

    public ResolvedClass getName() {
        return name;
    }

    public List<List<ResolvedInsn>> getArgs() {
        return args;
    }
}
