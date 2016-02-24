package weac.compiler;

/**
 * A phase in the compilation of the source code
 */
public abstract class CompilePhase<Input, Output> extends CompileUtils {

    public abstract Output process(Input input);

    public abstract Class<Input> getInputClass();

    public abstract Class<Output> getOutputClass();

}
