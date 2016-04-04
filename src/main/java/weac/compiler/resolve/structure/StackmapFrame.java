package weac.compiler.resolve.structure;

import weac.compiler.resolve.VariableMap;
import weac.compiler.utils.WeacType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StackmapFrame {

    private final List<WeacType> contents;
    private int stackSize;

    public StackmapFrame(int stackSize) {
        this.stackSize = stackSize;
        contents = new LinkedList<>();
    }

    public StackmapFrame(int stackSize, List<WeacType> contents) {
        this(stackSize);
        this.contents.addAll(contents);
    }

    public StackmapFrame(int stackSize, VariableMap map) {
        this(stackSize);
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
        StackmapFrame newFrame = new StackmapFrame(stackSize, contents);
        newFrame.contents.add(type);
        return newFrame;
    }

    public StackmapFrame append(Collection<WeacType> types) {
        StackmapFrame newFrame = new StackmapFrame(stackSize, contents);
        newFrame.contents.addAll(types);
        return newFrame;
    }

    public StackmapFrame same() {
        return new StackmapFrame(0, contents);
    }

    public StackmapFrame same1() {
        return new StackmapFrame(1, contents);
    }

    public StackmapFrame chop(int count) {
        return new StackmapFrame(stackSize, contents.subList(0, contents.size()-count));
    }

    public StackmapFrame setStackSize(int stackSize) {
        this.stackSize = stackSize;
        return this;
    }
}
