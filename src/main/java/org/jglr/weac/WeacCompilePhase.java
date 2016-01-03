package org.jglr.weac;

/**
 * A phase in the compilation of the source code
 */
public abstract class WeacCompilePhase<Input, Output> extends WeacCompileUtils {

    public abstract Output process(Input input);

    public abstract Class<Input> getInputClass();

    public abstract Class<Output> getOutputClass();

}
