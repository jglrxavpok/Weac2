package org.jglr.weac;

import org.jglr.weac.compile.WeacCompiler;
import org.jglr.weac.parse.WeacClassParser;
import org.jglr.weac.parse.WeacParser;
import org.jglr.weac.precompile.WeacPreCompiler;
import org.jglr.weac.process.WeacProcessor;
import org.jglr.weac.resolve.WeacResolver;
import org.jglr.weac.verify.WeacParsingVerifier;

import java.io.File;

public class WeacDefaultProcessor extends WeacProcessor {

    @Override
    public void initToolchain() {
        // Preprocessing -> Parsing (-> Verification) -> Pre compilation (breaking into pseudo-instructions) -> Type Resolution -> Compilation
        addToChain(new WeacPreProcessor());
        addToChain(new WeacParser());
        addToChain(new WeacParsingVerifier()); // control step in order to prevent impossible/unlogical classes
        addToChain(new WeacPreCompiler());
    }

}
