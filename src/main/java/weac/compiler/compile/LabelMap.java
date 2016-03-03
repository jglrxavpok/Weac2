package weac.compiler.compile;

import weac.compiler.precompile.Label;

import java.util.HashMap;
import java.util.Map;

public class LabelMap {

    private final Map<Integer, org.objectweb.asm.Label> map;

    public LabelMap() {
        map = new HashMap<>();
    }

    public org.objectweb.asm.Label get(Label label) {
        if(!map.containsKey(label.getIndex())) {
            map.put(label.getIndex(), new org.objectweb.asm.Label());
        }
        return map.get(label.getIndex());
    }
}
