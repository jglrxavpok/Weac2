package org.jglr.weac.verify;

import org.jglr.weac.WeacCompilePhase;
import org.jglr.weac.parse.structure.WeacParsedSource;

public class WeacParsingVerifier extends WeacCompilePhase<WeacParsedSource, WeacParsedSource> {
    @Override
    public WeacParsedSource process(WeacParsedSource source) {
        // TODO: Verifications
        return source;
    }

    @Override
    public Class<WeacParsedSource> getInputClass() {
        return WeacParsedSource.class;
    }

    @Override
    public Class<WeacParsedSource> getOutputClass() {
        return WeacParsedSource.class;
    }
}
