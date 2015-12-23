package org.jglr.weac;

public class WeacCompilePhase {

    protected void newError(String s, int lineIndex) {
        System.err.println("Error at line "+lineIndex+": "+s); // TODO: Collect errors
    }
}
