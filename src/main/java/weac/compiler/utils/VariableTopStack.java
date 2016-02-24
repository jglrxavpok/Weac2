package weac.compiler.utils;

import java.util.Stack;

public class VariableTopStack<Type> {

    private final Stack<Type> stack;
    private Type current;
    private int level;

    public VariableTopStack() {
        stack = new Stack<>();
    }

    public VariableTopStack<Type> push() {
        if(current == null) {
            throw new IllegalStateException("Cannot push value on stack if no value specified");
        }
        stack.push(current);
        level++;
        return this;
    }

    public int getSize() {
        return stack.size();
    }

    public Type pop() {
        Type popped = stack.pop();
        current = popped;
        level--;
        return popped;
    }

    public VariableTopStack setCurrent(Type value) {
        current = value;
        return this;
    }

    public Type getCurrent() {
        return current;
    }

    public int getLevel() {
        return level;
    }
}
