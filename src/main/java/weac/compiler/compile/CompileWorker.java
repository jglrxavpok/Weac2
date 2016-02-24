package weac.compiler.compile;

import weac.compiler.precompile.structure.PrecompiledClass;
import weac.compiler.precompile.structure.PrecompiledSource;
import weac.compiler.resolve.Resolver;
import weac.compiler.resolve.ResolvingContext;
import weac.compiler.resolve.structure.ResolvedSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CompileWorker implements Runnable {
    private final File output;
    private final PrecompiledSource source;
    private final List<PrecompiledClass> sideSources;

    public CompileWorker(File output, PrecompiledSource source, List<PrecompiledClass> sideSources) {
        this.output = output;
        this.source = source;
        this.sideSources = sideSources;
    }

    @Override
    public void run() {
        Resolver resolver = new Resolver();
        Compiler compiler = new Compiler();
        ResolvedSource resolvedSource = resolver.process(readImports(source, sideSources));
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

    private ResolvingContext readImports(PrecompiledSource from, List<PrecompiledClass> sideSources) {
        List<PrecompiledClass> finalList = new LinkedList<>();
        sideSources.stream()
                .filter(c -> c.packageName.equals("weac.lang")
                || from.imports.stream().filter(imp -> imp.importedType.equals(c.fullName)).count() != 0)
                .forEach(finalList::add);
        return new ResolvingContext(from, finalList.toArray(new PrecompiledClass[finalList.size()]));
    }
}
