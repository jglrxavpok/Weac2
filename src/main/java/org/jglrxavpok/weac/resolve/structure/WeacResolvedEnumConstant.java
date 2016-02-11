package org.jglrxavpok.weac.resolve.structure;

import org.jglrxavpok.weac.resolve.insn.WeacResolvedInsn;

import java.util.LinkedList;
import java.util.List;

public class WeacResolvedEnumConstant {

    public String name;

    public List<List<WeacResolvedInsn>> parameters;

    public WeacResolvedEnumConstant() {
        parameters = new LinkedList<>();
    }
}
