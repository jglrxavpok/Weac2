package weac.compiler;

import weac.compiler.compile.CompileWorker;
import weac.compiler.precompile.PrecompileWorker;
import weac.compiler.precompile.WritePrecompilationResultWorker;
import weac.compiler.precompile.structure.JavaImportedClass;
import weac.compiler.precompile.structure.PrecompiledClass;
import weac.compiler.precompile.structure.PrecompiledSource;
import weac.compiler.utils.Import;
import weac.compiler.utils.SourceCode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class WeacMonolith {

    private final File output;
    private final File standardClassesLocation;
    private List<PrecompiledClass> standardLib;
    private String stopAt;

    public WeacMonolith(File standardClassesLocation, String stopAt) {
        this(standardClassesLocation, null, stopAt);
    }

    public WeacMonolith(File standardClassesLocation, File outputFolder, String stopAt) {
        this.stopAt = stopAt;
        this.standardClassesLocation = standardClassesLocation;
        if(outputFolder == null) {
            outputFolder = new File("./monolith/");
        }
        this.output = outputFolder;
        if(!outputFolder.exists())
            outputFolder.mkdirs();
    }

    public void compile(SourceCode... sources) throws IOException, TimeoutException {
        initStandardLib();
        int maxThreads = 15;
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        PrecompileWorker[] precompiledWorkers = new PrecompileWorker[sources.length];
        int index = 0;
        for(SourceCode source : sources) {
            PrecompileWorker worker = new PrecompileWorker(source);
            precompiledWorkers[index++] = worker;
            executor.execute(worker);
        }
        executor.shutdown();
        if(stopAt.equals("precompilation")) {
            try {
                if(executor.awaitTermination(1000L, TimeUnit.HOURS)) {
                    executor = Executors.newFixedThreadPool(maxThreads);
                    for(PrecompileWorker worker : precompiledWorkers) {
                        executor.execute(new WritePrecompilationResultWorker(output, worker.getResult()));
                    }
                    executor.shutdown();
                    if(!executor.awaitTermination(1000L, TimeUnit.HOURS)) {
                        throw new TimeoutException();
                    }
                } else {
                    throw new TimeoutException();
                }
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        } else {
            try {
                if (executor.awaitTermination(1000L, TimeUnit.HOURS)) {
                    executor = Executors.newFixedThreadPool(maxThreads);
                    for (PrecompileWorker worker : precompiledWorkers) {
                        executor.execute(new CompileWorker(output, worker.getResult(), getSideClasses(worker, precompiledWorkers)));
                    }
                    executor.shutdown();
                    if (!executor.awaitTermination(1000L, TimeUnit.HOURS)) {
                        throw new TimeoutException();
                    }
                } else {
                    throw new TimeoutException();
                }
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    private void initStandardLib() throws IOException {
        standardLib = new ArrayList<>();
        if(standardClassesLocation != null) {
            System.out.println("Reading standard lib from "+standardClassesLocation.getAbsolutePath());
            // source children
            File[] children = standardClassesLocation.listFiles(f -> !f.isDirectory() && f.getName().endsWith(".ws"));
            if(children != null) {
                for (File child : children) {
                    PrecompilationProcessor processor = new PrecompilationProcessor();
                    standardLib.addAll(((PrecompiledSource) processor.process(new SourceCode(child.getName(), read(child)))).classes);
                }
            }
        }

        // TODO: already compiled children
    }

    private String read(File f) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(f.toURI()));
        return new String(bytes, "UTF-8");
    }

    private List<PrecompiledClass> getSideClasses(PrecompileWorker worker, PrecompileWorker[] allWorkers) {
        List<PrecompiledClass> sideSources = new ArrayList<>();
        for (PrecompileWorker w : allWorkers) {
            if (w != worker) {
                sideSources.addAll(w.getResult().classes);
            }
        }

        for(Import imp : worker.getResult().imports) {
            try {
                Class<?> javaClass = Class.forName(imp.importedType, false, getClass().getClassLoader());
                sideSources.add(new JavaImportedClass(javaClass));
                System.out.println("imported java class: "+imp.importedType);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        standardLib.forEach(s -> {
            for(PrecompiledClass clazz : worker.getResult().classes) {
                if ((clazz.packageName + clazz.name).equals(s.packageName + s.name)) {
                    return;
                }
            }
            for (PrecompiledClass side : sideSources) {
                // check if not already in sideSources
                if((side.packageName+side.name).equals(s.packageName+s.name)) {
                    return;
                }
            }
            sideSources.add(s);
            System.out.println("Added standard lib class: "+s.packageName+"."+s.name);
        });

        return sideSources;
    }

}
