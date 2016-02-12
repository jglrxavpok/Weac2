package org.jglrxavpok.weac.compile;

import org.jglrxavpok.weac.precompile.structure.WeacPrecompiledClass;
import org.jglrxavpok.weac.precompile.structure.WeacPrecompiledSource;
import org.jglrxavpok.weac.resolve.WeacResolver;
import org.jglrxavpok.weac.resolve.WeacResolvingContext;
import org.jglrxavpok.weac.resolve.structure.WeacResolvedSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WeacCompileWorker implements Runnable {
    private final File output;
    private final WeacPrecompiledSource source;
    private final List<WeacPrecompiledClass> sideSources;

    public WeacCompileWorker(File output, WeacPrecompiledSource source, List<WeacPrecompiledClass> sideSources) {
        this.output = output;
        this.source = source;
        this.sideSources = sideSources;
    }

    @Override
    public void run() {
        WeacResolver resolver = new WeacResolver();
        WeacCompiler compiler = new WeacCompiler();
        WeacResolvedSource resolvedSource = resolver.process(readImports(source, sideSources));
        Map<String, byte[]> classesBytecode = compiler.process(resolvedSource);
        for(String className : classesBytecode.keySet()) {
            File file = new File(output, className.replace(".", "/")+".class");
            if(!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            try {
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                byte[] bytecode = classesBytecode.get(className);
                if(/*debug*/true) {
                    try {
                        PrintWriter pw = new PrintWriter(System.out);
                        CheckClassAdapter.verify(new ClassReader(bytecode), true, pw);
                    } catch (Exception e) {
                        throw new RuntimeException("Error while compiling "+className, e);
                    }
                }
                out.write(bytecode);
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private WeacResolvingContext readImports(WeacPrecompiledSource from, List<WeacPrecompiledClass> sideSources) {
        List<WeacPrecompiledClass> finalList = new LinkedList<>();
        sideSources.stream()
                .filter(c -> c.packageName.equals("weac.lang")
                || from.imports.stream().filter(imp -> imp.importedType.equals(c.fullName)).count() != 0)
                .forEach(finalList::add);
        return new WeacResolvingContext(from, finalList.toArray(new WeacPrecompiledClass[finalList.size()]));
    }
}
