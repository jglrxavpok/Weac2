package weac.compiler.utils;

import joptsimple.*;
import weac.compiler.WeaCCCore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

import static java.util.Arrays.*;

public class WeaCC {

    public static void main(String[] args) throws IOException, TimeoutException {
        OptionParser parser = new OptionParser();
        parser.formatHelpWith(new BuiltinHelpFormatter(80, 2));
        parser.acceptsAll(asList("help", "h"), "Prints out the help");
        parser.acceptsAll(asList("encoding", "e"), "Selects the encoding in which the files will be read")
                .withRequiredArg().ofType(String.class).defaultsTo("UTF-8");
        parser.acceptsAll(asList("out", "o"), "Chooses the output folder, will be created if needed")
                .withRequiredArg().ofType(File.class).defaultsTo(new File("./classes"));
        parser.acceptsAll(asList("stdl", "standardLib"), "Chooses the folder from which to get the standard library. Must already exist")
                .withRequiredArg().ofType(File.class).defaultsTo(new File("./stdl"));
        parser.acceptsAll(asList("compilestdl", "compileStandardLib"), "Compiles everything single class file of the standard library into the output folder. Must already exist");
        parser.accepts("stopAt", "Stops at the given step. Must be one of 'resolution', 'precompilation', 'compilation'")
                .withRequiredArg().defaultsTo("compilation");
        parser.nonOptions("Source files location relative to current folder").ofType(File.class);
        if(args != null && args.length > 0) {
            OptionSet options = parser.parse(args);
            if(options.has("help")) {
                parser.printHelpOn(System.out);
            } else {
                File output = (File) options.valueOf("out");
                File standardLib = (File) options.valueOf("standardLib");
                if(!output.exists() || !output.isDirectory()) {
                    output.mkdirs();
                }
                if(!standardLib.exists() || !standardLib.isDirectory()) {
                    throw new FileNotFoundException("Standard lib location \""+standardLib.getAbsolutePath()+"\" is not a valid folder name. The folder must also exist");
                }
                if(options.has("compileStandardLib")) {
                    WeaCCCore monolith = new WeaCCCore(null, output, (String) options.valueOf("stopAt"));
                    List<File> toCompile = new ArrayList<>();
                    File[] children = standardLib.listFiles();
                    if(children != null) {
                        Collections.addAll(toCompile, children);
                        System.out.println("Compiling standard lib: "+Arrays.toString(children));
                        monolith.compile(toSourceCodes(toCompile, (String) options.valueOf("encoding")));
                    } else {
                        throw new FileNotFoundException("Standard lib location \""+standardLib.getAbsolutePath()+"\" is empty");
                    }
                } else {
                    WeaCCCore monolith = new WeaCCCore(standardLib, output, (String) options.valueOf("stopAt"));
                    List<File> toCompile = new ArrayList<>();
                    options.nonOptionArguments()
                            .forEach(o -> toCompile.add((File)o));
                    monolith.compile(toSourceCodes(toCompile, (String) options.valueOf("encoding")));
                }
            }
        } else {
            System.out.println("Need help?");
            parser.printHelpOn(System.out);
        }
    }

    private static SourceCode[] toSourceCodes(List<File> toCompile, String encoding) throws IOException {
        SourceCode[] result = new SourceCode[toCompile.size()];
        String[] contents = read(toCompile, encoding);
        for (int i = 0; i < result.length; i++) {
            result[i] = new SourceCode(toCompile.get(i).getName(), contents[i]);
        }
        return result;
    }

    private static String[] read(List<File> toCompile, String encoding) throws IOException {
        ExecutorService executor = Executors.newCachedThreadPool();
        String[] values = new String[toCompile.size()];
        Reader[] readers = new Reader[values.length];
        int index = 0;
        for(File f : toCompile) {
            Reader reader = new Reader(f, encoding);
            readers[index++] = reader;
            executor.execute(reader);
        }
        executor.shutdown();
        try {
            if(executor.awaitTermination(1000L, TimeUnit.HOURS)) {
                for(int i = 0;i<readers.length;i++) {
                    values[i] = readers[i].getResult();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return values;
    }

    private static class Reader implements Runnable {

        private final File file;
        private final String encoding;
        private String result;

        public Reader(File f, String encoding) {
            this.file = f;
            this.encoding = encoding;
        }

        @Override
        public void run() {
            try {
                result = new String(Files.readAllBytes(file.toPath()), encoding);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getResult() {
            return result;
        }
    }
}
