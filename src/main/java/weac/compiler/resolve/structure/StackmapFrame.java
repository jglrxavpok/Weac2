package weac.compiler.resolve.structure;

import weac.compiler.resolve.VariableMap;
import weac.compiler.utils.WeacType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StackmapFrame {

    private final List<WeacType> contents;

    public StackmapFrame() {
        contents = new LinkedList<>();
    }

    public StackmapFrame(List<WeacType> contents) {
        this();
        this.contents.addAll(contents);
    }

    public StackmapFrame(VariableMap map) {
        this();
        int localCount = map.getCurrentLocalIndex();
        for (int i = 0; i < localCount; i++) {
            contents.add(map.getLocalType(map.getLocalName(i)));
        }
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("StackMapFrame[");
        for (int i = 0; i < contents.size(); i++) {
            if(i != 0)
                buffer.append(' ');
            buffer.append(contents.get(i).toString());
        }
        buffer.append(']');
        return buffer.toString();
    }

    public StackmapFrame append(String type) {
        return append(new WeacType(WeacType.JOBJECT_TYPE, type, true));
    }

    public StackmapFrame append(WeacType type) {
        StackmapFrame newFrame = new StackmapFrame(contents);
        newFrame.contents.add(type);
        return newFrame;
    }

    public StackmapFrame append(Collection<WeacType> types) {
        StackmapFrame newFrame = new StackmapFrame(contents);
        newFrame.contents.addAll(types);
        return newFrame;
    }
}
