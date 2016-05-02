package weac.compiler.resolve.structure;

import weac.compiler.precompile.structure.PrecompiledMethod;
import weac.compiler.resolve.ConstructorInfos;
import weac.compiler.resolve.insn.ResolvedInsn;

import java.util.LinkedList;
import java.util.List;

public class ResolvedEnumConstant {

    public String name;

    public List<List<ResolvedInsn>> parameters;

    /**
     * The constructor used by this enum constant
     */
    public ConstructorInfos usedConstructor;
    public int ordinal;

    public ResolvedEnumConstant() {
        parameters = new LinkedList<>();
    }
}
