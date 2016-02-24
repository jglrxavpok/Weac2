package weac.compiler.precompile;

import weac.compiler.PrecompilationProcessor;
import weac.compiler.precompile.structure.PrecompiledSource;

public class PrecompileWorker implements Runnable {
    private final String source;
    private PrecompiledSource result;

    public PrecompileWorker(String source) {
        this.source = source;
    }

    @Override
    public void run() {
        try {
            PrecompilationProcessor processor = new PrecompilationProcessor();
            result = (PrecompiledSource) processor.process(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PrecompiledSource getResult() {
        return result;
    }
}
