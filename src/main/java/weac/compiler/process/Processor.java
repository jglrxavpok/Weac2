package weac.compiler.process;

import weac.compiler.CompilePhase;

import java.util.ArrayList;
import java.util.List;

public abstract class Processor {

    private List<CompilePhase> toolchain;

    public Processor() {
        toolchain = new ArrayList<>();
        initToolchain();
    }

    protected abstract void initToolchain();

    protected void addToChain(CompilePhase<?, ?> phase) {
        if(toolchain.isEmpty()) {
            toolchain.add(phase);
        } else {
            CompilePhase last = toolchain.get(toolchain.size()-1);
            if(isCompatible(last, phase)) {
                toolchain.add(phase);
            } else {
                throw new RuntimeException("Uncompatible phases in toolchain: current end output is "+last.getOutputClass().getSimpleName()+" while new entry input is "+phase.getOutputClass().getSimpleName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected boolean isCompatible(CompilePhase last, CompilePhase newPhase) {
        return last.getOutputClass().isAssignableFrom(newPhase.getInputClass());
    }

    @SuppressWarnings("unchecked")
    public Object process(String source) {
        Object currentResult = source;
        for(CompilePhase phase : toolchain) {
            currentResult = phase.process(currentResult);
        }
        return currentResult;
    }
}
