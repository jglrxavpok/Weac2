package weac.compiler.resolve;

import weac.compiler.resolve.insn.ResolvedInsn;
import weac.compiler.resolve.values.Value;
import weac.compiler.utils.WeacType;

import java.util.List;
import java.util.Map;
import java.util.Stack;

public abstract class NativeCodeResolver {

    private final Resolver owner;
    private final WeacType currentClassType;
    private final ResolvingContext context;

    public NativeCodeResolver(Resolver owner, WeacType currentClassType, ResolvingContext context) {
        this.owner = owner;
        this.currentClassType = currentClassType;
        this.context = context;
    }

    public ResolvingContext getContext() {
        return context;
    }

    public WeacType getCurrentClassType() {
        return currentClassType;
    }

    public Resolver getOwner() {
        return owner;
    }

    public abstract void resolve(String code, WeacType currentType, VariableMap map, Map<WeacType, VariableMap> variableMaps, Stack<Value> valueStack, List<ResolvedInsn> insns);
}
