package org.jglrxavpok.weac.resolve.structure;

import org.jglrxavpok.weac.resolve.insn.WeacResolvedInsn;

import java.util.LinkedList;
import java.util.List;

public class WeacResolvedAnnotation {

    private final WeacResolvedClass name;
    public List<List<WeacResolvedInsn>> args;

    public WeacResolvedAnnotation(WeacResolvedClass type) {
        this.name = type;
        args = new LinkedList<>();
    }

    public WeacResolvedClass getName() {
        return name;
    }

    public List<List<WeacResolvedInsn>> getArgs() {
        return args;
    }
}
