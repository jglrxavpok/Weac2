package weac.compiler.resolve.structure;

import weac.compiler.resolve.insn.ResolvedInsn;

import java.util.LinkedList;
import java.util.List;

public class ResolvedEnumConstant {

    public String name;

    public List<List<ResolvedInsn>> parameters;

    public ResolvedEnumConstant() {
        parameters = new LinkedList<>();
    }
}
