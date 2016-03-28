package weac.compiler.compile;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import weac.compiler.utils.WeacType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PrimitiveCastCompiler {

    private final HashMap<String, List<String>> possibilities;
    private final HashMap<String, List<Integer>> possibilitiesOps;

    public PrimitiveCastCompiler() {
        possibilities = new HashMap<>();
        possibilitiesOps = new HashMap<>();
        Field[] fields = Opcodes.class.getFields();
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

                    System.out.println("Found cast: "+name+" ("+from+" -> "+to+")");

                    possibilitiesOps.put(from, possibleOpcodes);
                    possibilities.put(from, possibleNames);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void compile(WeacType from, WeacType to, MethodVisitor writer) {

    }
}
