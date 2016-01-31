package org.jglr.weac.precompile.structure;

import org.jglr.weac.precompile.insn.WeacPrecompiledInsn;

import java.util.LinkedList;
import java.util.List;

public class WeacPrecompiledAnnotation {

    private final String name;
    public List<List<WeacPrecompiledInsn>> args;

    public WeacPrecompiledAnnotation(String name) {
        this.name = name;
        args = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public List<List<WeacPrecompiledInsn>> getArgs() {
        return args;
    }
}
