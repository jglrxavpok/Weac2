package org.jglr.weac;

import org.jglr.weac.compile.WeacCompiler;
import org.jglr.weac.precompile.structure.WeacPrecompiledClass;
import org.jglr.weac.precompile.structure.WeacPrecompiledSource;
import org.jglr.weac.resolve.WeacResolver;
import org.jglr.weac.resolve.structure.WeacResolvedSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class WeacMonolith {

    private final File output;

    public WeacMonolith() {
        this(null);
    }

    public WeacMonolith(File outputFolder) {
        if(outputFolder == null) {
            outputFolder = new File("./monolith/");
        }
        this.output = outputFolder;
        if(!outputFolder.exists())
            outputFolder.mkdirs();
    }

    public void compile(String source) throws IOException {
        WeacDefaultProcessor processor = new WeacDefaultProcessor();
        WeacResolver resolver = new WeacResolver();
        WeacCompiler compiler = new WeacCompiler();

        WeacPrecompiledSource precompiledSource = (WeacPrecompiledSource) processor.process(source);
        WeacResolvedSource resolvedSource = resolver.process(precompiledSource, readImports(precompiledSource));
        Map<String, byte[]> classesBytecode = compiler.process(resolvedSource);
        for(String className : classesBytecode.keySet()) {
            File file = new File(output, className.replace(".", "/")+".wc");
            FileOutputStream out = new FileOutputStream(file);
            out.write(classesBytecode.get(className));
            out.flush();
            out.close();
        }

    }

    private WeacPrecompiledClass[] readImports(WeacPrecompiledSource precompiledSource) {
        return new WeacPrecompiledClass[0];
    }
}
