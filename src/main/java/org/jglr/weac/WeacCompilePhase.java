package org.jglr.weac;

import org.jglr.weac.utils.WeacModifier;

import java.util.List;

/**
 * A phase in the compilation of the source code
 */
public abstract class WeacCompilePhase<Input, Output> extends WeacCompileUtils {

    public abstract Output process(Input input);

    public abstract Class<Input> getInputClass();

    public abstract Class<Output> getOutputClass();

}
