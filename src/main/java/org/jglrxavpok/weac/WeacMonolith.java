package org.jglrxavpok.weac;

import org.jglrxavpok.weac.compile.WeacCompileWorker;
import org.jglrxavpok.weac.precompile.WeacPrecompileWorker;
import org.jglrxavpok.weac.precompile.WritePrecompilationResultWorker;
import org.jglrxavpok.weac.precompile.structure.JavaImportedClass;
import org.jglrxavpok.weac.precompile.structure.WeacPrecompiledClass;
import org.jglrxavpok.weac.precompile.structure.WeacPrecompiledSource;
import org.jglrxavpok.weac.utils.WeacImport;

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
    private List<WeacPrecompiledClass> standardLib;
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

    public void compile(String... sources) throws IOException, TimeoutException {
        initStandardLib();
        int maxThreads = 15;
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        WeacPrecompileWorker[] precompiledWorkers = new WeacPrecompileWorker[sources.length];
        int index = 0;
        for(String source : sources) {
            WeacPrecompileWorker worker = new WeacPrecompileWorker(source);
            precompiledWorkers[index++] = worker;
            executor.execute(worker);
        }
        executor.shutdown();
        if(stopAt.equals("precompilation")) {
            try {
                if(executor.awaitTermination(1000L, TimeUnit.HOURS)) {
                    executor = Executors.newFixedThreadPool(maxThreads);
                    for(WeacPrecompileWorker worker : precompiledWorkers) {
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
                    for (WeacPrecompileWorker worker : precompiledWorkers) {
                        executor.execute(new WeacCompileWorker(output, worker.getResult(), getSideClasses(worker, precompiledWorkers)));
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
                    standardLib.addAll(((WeacPrecompiledSource) processor.process(read(child))).classes);
                }
            }
        }

        // TODO: already compiled children
    }

    private String read(File f) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(f.toURI()));
        return new String(bytes, "UTF-8");
    }

    private List<WeacPrecompiledClass> getSideClasses(WeacPrecompileWorker worker, WeacPrecompileWorker[] allWorkers) {
        List<WeacPrecompiledClass> sideSources = new ArrayList<>();
        for (WeacPrecompileWorker w : allWorkers) {
            if (w != worker) {
                sideSources.addAll(w.getResult().classes);
            }
        }

        for(WeacImport imp : worker.getResult().imports) {
            try {
                Class<?> javaClass = Class.forName(imp.importedType, false, getClass().getClassLoader());
                sideSources.add(new JavaImportedClass(javaClass));
                System.out.println("imported java class: "+imp.importedType);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        standardLib.forEach(s -> {
            for(WeacPrecompiledClass clazz : worker.getResult().classes) {
                if ((clazz.packageName + clazz.name).equals(s.packageName + s.name)) {
                    return;
                }
            }
            for (WeacPrecompiledClass side : sideSources) {
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
