package weac.compiler.optimize;

import weac.compiler.CompilePhase;
import weac.compiler.precompile.Label;
import weac.compiler.resolve.insn.*;
import weac.compiler.resolve.structure.ResolvedClass;
import weac.compiler.resolve.structure.ResolvedField;
import weac.compiler.resolve.structure.ResolvedMethod;
import weac.compiler.resolve.structure.ResolvedSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class Optimizer extends CompilePhase<ResolvedSource, ResolvedSource> {

    private final ArrayList<OptimizationLayer> layers;

    public Optimizer() {
        layers = new ArrayList<>();
        layers.add(new NeutralElementsLayer());
        layers.add(new NullChecksLayer());
        layers.add(new TailCallLayer());
        layers.add(new UnnecessaryLoadLayer());
    }

    @Override
    public ResolvedSource process(ResolvedSource resolvedSource) {
        resolvedSource.classes.forEach(this::optimizeClassMembers);
        return resolvedSource;
    }

    private void optimizeClassMembers(ResolvedClass resolvedClass) {
        resolvedClass.fields.forEach(f -> optimizeField(f, resolvedClass));
        resolvedClass.methods.forEach(m -> optimizeMethod(m, resolvedClass));
    }

    private void optimizeField(ResolvedField resolvedField, ResolvedClass owner) {
        optimize(null, resolvedField, owner, resolvedField.defaultValue);
    }

    private List<ResolvedInsn> optimize(ResolvedMethod method, ResolvedField field, ResolvedClass owner, List<ResolvedInsn> insns) {
        for (OptimizationLayer o : layers) {
            insns = o.optimize(method, field, owner, insns);
        }
        return insns;
    }

    private void optimizeMethod(ResolvedMethod method, ResolvedClass owner) {
        List<ResolvedInsn> result = optimize(method, null, owner, method.instructions);
        method.instructions.clear();
        method.instructions.addAll(result);
    }

    @Override
    public Class<ResolvedSource> getInputClass() {
        return ResolvedSource.class;
    }

    @Override
    public Class<ResolvedSource> getOutputClass() {
        return ResolvedSource.class;
    }
}
