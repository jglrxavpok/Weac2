package weac.compiler.compile;

import org.jglr.flows.collection.DoubleKeyMap;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import weac.compiler.utils.WeacType;

import java.lang.reflect.Field;
import java.util.*;

public class PrimitiveCastCompiler {

    private final String[] types;
    private final DoubleKeyMap<String, String, List<Integer>> paths;

    public PrimitiveCastCompiler() {
        HashMap<String, List<String>> possibilities = new HashMap<>();
        HashMap<String, List<Integer>> possibilitiesOps = new HashMap<>();
        paths = new DoubleKeyMap<>();

        Field[] fields = Opcodes.class.getFields();
        types = new String[] {
                "I", "S", "D", "F", "L", "B", "C"
        };

        for(Field field : fields) {
            String name = field.getName();
            if(name.length() >= 3 && name.charAt(1) == '2') {
                String from = String.valueOf(name.charAt(0));
                String to = String.valueOf(name.charAt(2));
                List<String> possibleNames = possibilities.getOrDefault(from, new ArrayList<>());
                List<Integer> possibleOpcodes = possibilitiesOps.getOrDefault(from, new ArrayList<>());
                try {
                    possibleOpcodes.add(field.getInt(null));
                    possibleNames.add(to);

                  //  System.out.println("Found cast: "+name+" ("+from+" -> "+to+")");

                    possibilitiesOps.put(from, possibleOpcodes);
                    possibilities.put(from, possibleNames);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        for(String type : types) {
            for(String other : types) {
                if(!other.equals(type)) {
                    List<Integer> output = new ArrayList<>();
                    initPath(type, other, possibilities, possibilitiesOps, output, new ArrayList<>());
                    System.out.println("Found path from "+type+" to "+other+": "+ Arrays.toString(output.toArray()));
                    paths.put(type, other, output);
                }
            }
        }
    }

    private boolean initPath(String type, String to, HashMap<String, List<String>> possibilitiesMap, HashMap<String, List<Integer>> possibilitiesOpsMap, List<Integer> output, List<String> wentThrough) {
        wentThrough.add(type);
        List<List<Integer>> possiblePaths = new ArrayList<>();
        List<String> possibilities = possibilitiesMap.get(type);
        List<Integer> possibilitiesOps = possibilitiesOpsMap.get(type);
        if(possibilities == null || possibilitiesOps == null)
            return false;
        if(type.equals(to)) {
            return false;
        }
        for (int i = 0; i < possibilities.size(); i++) {
            String possibility = possibilities.get(i);
            if(possibility.equals(type)) {
                return false;
            }
            int correspondingOpcode = possibilitiesOps.get(i);
            if(possibility.equals(to)) {
                output.add(correspondingOpcode);
                return true;
            } else {
                List<Integer> current = new ArrayList<>();
                if(!wentThrough.contains(possibility) && initPath(possibility, to, possibilitiesMap, possibilitiesOpsMap, current, wentThrough)) {
                    possiblePaths.add(current);
                    current.add(correspondingOpcode);
                }
            }
        }

        // take shortest path
        Optional<List<Integer>> shortest = possiblePaths.stream()
                .sorted((a, b) -> Integer.compare(a.size(), b.size()))
                .findFirst();

        if(shortest.isPresent()) {
            output.addAll(shortest.get());
            return true;
        }
        return false;
    }

    public void compile(String jvmTypeFrom, String jvmTypeTo, MethodVisitor writer) {
        if(jvmTypeFrom.equals("B") || jvmTypeFrom.equals("C")) { // promote byte and chars to ints
            compile("I", jvmTypeTo, writer);
        } else if(paths.containsKey(jvmTypeFrom, jvmTypeTo)) {
            List<Integer> list = paths.get(jvmTypeFrom, jvmTypeTo);
            System.out.println("CAST "+jvmTypeFrom+" -> "+jvmTypeTo);
            list.forEach(i -> {
                writer.visitInsn(i);
                System.out.println("WROTE "+i);
            });
        } else {
            System.out.println(">><< Not found: "+jvmTypeFrom+" -> "+jvmTypeTo);
        }
    }
}
