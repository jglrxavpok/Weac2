package org.jglrxavpok.weac.parse.structure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WeacParsedAnnotation {

    private final String name;
    public List<String> args;

    public WeacParsedAnnotation(String name) {
        this.name = name;
        args = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public List<String> getArgs() {
        return args;
    }
}
