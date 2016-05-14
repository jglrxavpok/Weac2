package weac.compiler.targets.jvm;

import weac.compiler.resolve.NativeCodeResolver;
import weac.compiler.resolve.Resolver;
import weac.compiler.resolve.ResolvingContext;
import weac.compiler.resolve.TypeResolver;
import weac.compiler.targets.jvm.compile.JVMCompiler;
import weac.compiler.targets.TargetCompiler;
import weac.compiler.targets.WeacTarget;
import weac.compiler.targets.jvm.resolve.BytecodeResolver;
import weac.compiler.targets.jvm.resolve.JVMTypeResolver;
import weac.compiler.utils.WeacType;

public class JVMTarget implements WeacTarget {

    @Override
    public String getHumanReadableName() {
        return "Java Virtual Machine";
    }

    @Override
    public String getIdentifier() {
        return "jvm";
    }

    @Override
    public TargetCompiler newCompiler() {
        return new JVMCompiler();
    }

    @Override
    public TypeResolver newTypeResolver(Resolver resolver) {
        return new JVMTypeResolver(resolver);
    }

    @Override
    public NativeCodeResolver newNativeCodeResolver(Resolver resolver, WeacType selfType, ResolvingContext context) {
        return new BytecodeResolver(resolver, selfType, context);
    }
}
