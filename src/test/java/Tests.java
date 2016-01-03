import org.jglr.weac.WeacDefaultProcessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public abstract class Tests {

    public String read(String filename) throws IOException {
        InputStream stream = WeacDefaultProcessor.class.getResourceAsStream("/"+filename);
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
