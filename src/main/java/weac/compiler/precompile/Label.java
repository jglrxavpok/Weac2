package weac.compiler.precompile;

public class Label {

    private final int index;

    public Label(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Label && ((Label) obj).getIndex() == getIndex();
    }

    @Override
    public String toString() {
        return "Label["+index+"]";
    }
}
