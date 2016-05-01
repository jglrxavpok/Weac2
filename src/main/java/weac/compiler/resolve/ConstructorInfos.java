package weac.compiler.resolve;

import weac.compiler.utils.Identifier;
import weac.compiler.utils.WeacType;

import java.util.LinkedList;
import java.util.List;

public class ConstructorInfos {

    public List<Identifier> argNames;
    public List<WeacType> argTypes;

    public ConstructorInfos() {
        argNames = new LinkedList<>();
        argTypes = new LinkedList<>();
    }
}
