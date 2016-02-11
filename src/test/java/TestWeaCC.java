import joptsimple.OptionException;
import org.jglrxavpok.weac.utils.WeaCC;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TestWeaCC extends Tests {

    @Test
    public void help() throws IOException, TimeoutException {
        WeaCC.main(new String[] {"--help"});
    }

    @Test(expected = OptionException.class)
    public void outWithNoArg() throws IOException, TimeoutException {
        WeaCC.main(new String[] {"--out"});
    }

    @Test(expected = OptionException.class)
    public void standardLibLocationWithNoArg() throws IOException, TimeoutException {
        WeaCC.main(new String[] {"--stdl"});
    }

    @Test(expected = FileNotFoundException.class)
    public void invalidStdl() throws IOException, TimeoutException {
        WeaCC.main(new String[] {"--stdl", "notExistingFolder/neither"});
    }

    @Test
    public void simpleCompile() throws IOException, TimeoutException {
        File output = new File(".", "monolith");
        empty(new File(output, "tests"));
        WeaCC.main(new String[] {"--out", output.getPath(), "--stdl", "src/main/resources/weac/lang", "src/test/resources/tests/HelloWorld.ws"});
        assertEquals(asList(new File("HelloWorld.class")), asList(new File(output, "tests").listFiles()));
    }

    @Test
    public void compileStdl() throws IOException, TimeoutException {
        File output = new File(".", "monolith");
        empty(new File(output, "weac/lang"));
        WeaCC.main(new String[] {"--out", output.getPath(), "--compilestdl", "--stdl", "src/main/resources/weac/lang/"});
        assertEquals(asList(new File("HelloWorld.class")), asList(output.listFiles()));
    }

    private void empty(File output) {
        File[] subFiles = output.listFiles();
        if(subFiles != null) {
            for (File subFile : subFiles) {
                if(subFile.isDirectory()) {
                    empty(subFile);
                }
                subFile.delete();
            }
        }
    }
}
