package org.jglrxavpok.weac.precompile;

import org.jglrxavpok.weac.WeacDefaultProcessor;
import org.jglrxavpok.weac.precompile.structure.WeacPrecompiledSource;

public class WeacPrecompileWorker implements Runnable {
    private final String source;
    private WeacPrecompiledSource result;

    public WeacPrecompileWorker(String source) {
        this.source = source;
    }

    @Override
    public void run() {
        try {
            WeacDefaultProcessor processor = new WeacDefaultProcessor();
            result = (WeacPrecompiledSource) processor.process(source);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WeacPrecompiledSource getResult() {
        return result;
    }
}
