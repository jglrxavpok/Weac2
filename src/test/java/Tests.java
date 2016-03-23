import weac.compiler.PrecompilationProcessor;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class Tests {

    public String read(String filename) throws IOException {
        return new String(readRaw(filename, true), "UTF-8");
    }

    public byte[] readRaw(String filename, boolean inClasspath) throws IOException {
        InputStream stream = inClasspath ? PrecompilationProcessor.class.getResourceAsStream("/"+filename) : new FileInputStream(filename);
        byte[] buffer = new byte[8096];
        int i;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while((i = stream.read(buffer)) != -1) {
            out.write(buffer, 0, i);
        }
        out.flush();
        out.close();
        return out.toByteArray();
    }
}
