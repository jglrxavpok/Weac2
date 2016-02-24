package weac.compiler.precompile;

import weac.compiler.PrecompilationProcessor;
import weac.compiler.precompile.structure.WeacPrecompiledSource;

public class WeacPrecompileWorker implements Runnable {
    private final String source;
    private WeacPrecompiledSource result;

    public WeacPrecompileWorker(String source) {
        this.source = source;
    }

    @Override
    public void run() {
        try {
            PrecompilationProcessor processor = new PrecompilationProcessor();
            result = (WeacPrecompiledSource) processor.process(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WeacPrecompiledSource getResult() {
        return result;
    }
}
