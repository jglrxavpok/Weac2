package weac.compiler.compile;

import org.jglr.flows.io.IndentableWriter;
import weac.compiler.optimize.Optimizer;
import weac.compiler.precompile.structure.PrecompiledClass;
import weac.compiler.precompile.structure.PrecompiledSource;
import weac.compiler.resolve.Resolver;
import weac.compiler.resolve.ResolvingContext;
import weac.compiler.resolve.structure.ResolvedClass;
import weac.compiler.resolve.structure.ResolvedSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import weac.compiler.targets.jvm.compile.JVMCompiler;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CompileWorker implements Runnable {
    private final File output;
    private final String stopAt;
    private final PrecompiledSource source;
    private final List<PrecompiledClass> sideSources;

    public CompileWorker(File output, String stopAt, PrecompiledSource source, List<PrecompiledClass> sideSources) {
        this.output = output;
        this.stopAt = stopAt;
        this.source = source;
        this.sideSources = sideSources;
    }

    @Override
    public void run() {
        Resolver resolver = new Resolver(source.target);
        JVMCompiler JVMCompiler = new JVMCompiler();
        Optimizer optimizer = new Optimizer();
        ResolvedSource resolvedSource = resolver.process(readImports(source, sideSources));
        resolvedSource = optimizer.process(resolvedSource);
        if(stopAt.equals("resolution")) {
            for(ResolvedClass c : resolvedSource.classes) {
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
        } else {

            try {
                Map<String, byte[]> classesBytecode = JVMCompiler.process(resolvedSource);
                for (String className : classesBytecode.keySet()) {
                    File file = new File(output, className.replace(".", "/") + ".class");
                    if (!file.getParentFile().exists())
                        file.getParentFile().mkdirs();
                    try {
                        file.createNewFile();
                        FileOutputStream out = new FileOutputStream(file);
                        byte[] bytecode = classesBytecode.get(className);
                        out.write(bytecode);
                        out.flush();
                        out.close();
                        try {
                            defineClass(className, bytecode);
                        } catch (InvocationTargetException | IllegalAccessException e) {
                         //   e.printStackTrace();
                        }
                        if (/*debug*/true) {
                            try {
                                PrintWriter pw = new PrintWriter(System.out);
                                CheckClassAdapter.verify(new ClassReader(bytecode), true, pw);
                            } catch (Exception e) {
                                throw new RuntimeException("Error while compiling " + className, e);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Throwable t) {
                System.err.println(">> THROWN");
                t.printStackTrace();
            }
        }
    }

    private static Class<?> defineClass(String name, byte[] classData) throws InvocationTargetException, IllegalAccessException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if(cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        try {
            Method m = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            m.setAccessible(true);
            Class<?> result = (Class<?>) m.invoke(cl, name, classData, 0, classData.length);
            return result;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
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
