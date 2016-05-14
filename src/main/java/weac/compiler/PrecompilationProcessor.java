package weac.compiler;

import weac.compiler.chop.Chopper;
import weac.compiler.precompile.PreCompiler;
import weac.compiler.process.Processor;
import weac.compiler.verify.ParsingVerifier;

public class PrecompilationProcessor extends Processor {

    @Override
    public void initToolchain() {
        // Preprocessing -> Chopping (-> Verification) -> Pre compilation (breaking into pseudo-instructions) -> Type Resolution -> Compilation
        addToChain(new PreProcessor());
        addToChain(new Chopper());
        addToChain(new ParsingVerifier()); // control step in order to prevent impossible/unlogical classes
        addToChain(new PreCompiler());
    }

}
