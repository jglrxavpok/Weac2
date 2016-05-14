package weac.compiler.targets.jvm.resolve;

import weac.compiler.resolve.NativeCodeResolver;
import weac.compiler.resolve.Resolver;
import weac.compiler.resolve.ResolvingContext;
import weac.compiler.resolve.VariableMap;
import weac.compiler.resolve.values.Value;
import weac.compiler.utils.WeacType;

import java.util.Map;
import java.util.Stack;

public class BytecodeResolver extends NativeCodeResolver {
    public BytecodeResolver(Resolver resolver, WeacType selfType, ResolvingContext context) {
        super(resolver, selfType, context);
    }

    @Override
    public void resolve(String code, WeacType currentType, VariableMap map, Map<WeacType, VariableMap> variableMaps, Stack<Value> valueStack) {
        System.out.println("!!! "+code);
    }
}
