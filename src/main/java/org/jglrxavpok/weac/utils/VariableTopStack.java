package org.jglrxavpok.weac.utils;

import java.util.Stack;

public class VariableTopStack<Type> {

    private final Stack<Type> stack;
    private Type current;

    public VariableTopStack() {
        stack = new Stack<>();
    }

    public VariableTopStack<Type> push() {
        if(current == null) {
            throw new IllegalStateException("Cannot push value on stack if no value specified");
        }
        stack.push(current);
        return this;
    }

    public Type pop() {
        Type popped = stack.pop();
        current = popped;
        return popped;
    }

    public VariableTopStack setCurrent(Type value) {
        current = value;
        return this;
    }

    public Type getCurrent() {
        return current;
    }
}
