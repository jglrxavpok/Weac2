package org.jglrxavpok.weac;

import org.jglrxavpok.weac.parse.WeacParser;
import org.jglrxavpok.weac.precompile.WeacPreCompiler;
import org.jglrxavpok.weac.process.WeacProcessor;
import org.jglrxavpok.weac.verify.WeacParsingVerifier;

public class PrecompilationProcessor extends WeacProcessor {

    @Override
    public void initToolchain() {
        // Preprocessing -> Parsing (-> Verification) -> Pre compilation (breaking into pseudo-instructions) -> Type Resolution -> Compilation
        addToChain(new WeacPreProcessor());
        addToChain(new WeacParser());
        addToChain(new WeacParsingVerifier()); // control step in order to prevent impossible/unlogical classes
        addToChain(new WeacPreCompiler());
    }

}
