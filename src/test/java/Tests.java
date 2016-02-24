import weac.compiler.PrecompilationProcessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class Tests {

    public String read(String filename) throws IOException {
        InputStream stream = PrecompilationProcessor.class.getResourceAsStream("/"+filename);
        byte[] buffer = new byte[8096];
        int i;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while((i = stream.read(buffer)) != -1) {
            out.write(buffer, 0, i);
        }
        out.flush();
        out.close();
        return new String(out.toByteArray(), "UTF-8");
    }
}
