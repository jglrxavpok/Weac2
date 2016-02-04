package org.jglr.weac.resolve;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;

public class VariableMap {

    private final HashMap<String, Integer> varIds;
    private final HashMap<Integer, String> varNames;

    public VariableMap() {
        varIds = new HashMap<>();
        varNames = new HashMap<>();
    }

    public int register(String name) {
        int index = getCurrentLocalIndex()+1;
        varIds.put(name, index);
        varNames.put(index, name);
        return index;
    }

    public int getCurrentLocalIndex() {
        Optional<Integer> op = varNames.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .findFirst();
        return op.orElse(0);
    }

    public boolean exists(String name) {
        return getIndex(name) != -1;
    }

    public String getName(int index) {
        return varNames.getOrDefault(index, "$INVALID ID$");
    }

    public int getIndex(String name) {
        return varIds.getOrDefault(name, -1);
    }
}
