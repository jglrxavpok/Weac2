package org.jglr.weac;

import org.jglr.weac.parse.WeacClassParser;
import org.jglr.weac.parse.WeacParser;
import org.jglr.weac.process.WeacProcessor;
import org.jglr.weac.verify.WeacParsingVerifier;

public class WeacDefaultProcessor extends WeacProcessor {
    @Override
    public void initToolchain() {
        addToChain(new WeacPreProcessor());
        addToChain(new WeacParser());
        addToChain(new WeacParsingVerifier()); // control step in order to prevent impossible/unlogical classes
    }
}
