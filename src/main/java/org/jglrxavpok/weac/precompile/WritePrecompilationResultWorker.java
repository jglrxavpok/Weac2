package org.jglrxavpok.weac.precompile;

import org.jglrxavpok.weac.precompile.structure.WeacPrecompiledClass;
import org.jglrxavpok.weac.precompile.structure.WeacPrecompiledSource;
import org.jglrxavpok.weac.utils.IndentableWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WritePrecompilationResultWorker implements Runnable {
    private final File output;
    private final WeacPrecompiledSource result;

    public WritePrecompilationResultWorker(File output, WeacPrecompiledSource result) {
        this.output = output;
        this.result = result;
    }

    @Override
    public void run() {
        for(WeacPrecompiledClass c : result.classes) {
            String fileName = c.fullName.replace(".", "/");
            File outputFile = new File(output, fileName+".preweac");
            if(!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }

            try {
                IndentableWriter writer = new IndentableWriter(new FileWriter(outputFile));
                c.writeTo(writer);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
