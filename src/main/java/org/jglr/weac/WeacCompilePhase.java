package org.jglr.weac;

public class WeacCompilePhase {

    protected void newError(String s, int lineIndex) {
        System.err.println("Error at line "+lineIndex+": "+s); // TODO: Collect errors
    }

    protected String readUntilSpace(String arg) {
        int end = arg.indexOf(' ');
        if(end < 0)
            end = arg.length();
        return arg.substring(0, end);
    }

    protected String trimStartingSpace(String l) {
        while(l.startsWith(" ")) {
            l = l.substring(1);
        }
        while(l.startsWith("\t")) {
            l = l.substring(1);
        }
        return l;
    }
}
