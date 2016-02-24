package weac.compiler;

import weac.compiler.parse.WeacParser;
import weac.compiler.precompile.WeacPreCompiler;
import weac.compiler.process.WeacProcessor;
import weac.compiler.verify.WeacParsingVerifier;

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
