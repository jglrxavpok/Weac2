package org.jglr.weac.process;

import org.jglr.weac.WeacCompilePhase;

import java.util.ArrayList;
import java.util.List;

public abstract class WeacProcessor {

    private List<WeacCompilePhase> toolchain;

    public WeacProcessor() {
        toolchain = new ArrayList<>();
        initToolchain();
    }

    protected abstract void initToolchain();

    protected void addToChain(WeacCompilePhase<?, ?> phase) {
        if(toolchain.isEmpty()) {
            toolchain.add(phase);
        } else {
            WeacCompilePhase last = toolchain.get(toolchain.size()-1);
            if(isCompatible(last, phase)) {
                toolchain.add(phase);
            } else {
                throw new RuntimeException("Uncompatible phases in toolchain: current end output is "+last.getOutputClass().getSimpleName()+" while new entry input is "+phase.getOutputClass().getSimpleName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected boolean isCompatible(WeacCompilePhase last, WeacCompilePhase newPhase) {
        return last.getOutputClass().isAssignableFrom(newPhase.getInputClass());
    }

    @SuppressWarnings("unchecked")
    public Object process(String source) {
        Object currentResult = source;
        for(WeacCompilePhase phase : toolchain) {
            currentResult = phase.process(currentResult);
        }
        return currentResult;
    }
}
