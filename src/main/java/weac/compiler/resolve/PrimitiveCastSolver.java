package weac.compiler.resolve;

import weac.compiler.CompileUtils;
import weac.compiler.targets.jvm.JVMWeacTypes;
import weac.compiler.utils.WeacType;

import java.util.*;

public class PrimitiveCastSolver extends CompileUtils {

    private Map<WeacType, List<WeacType>> casts;
    private Map<WeacType, Integer> sizes;

    public PrimitiveCastSolver() {
        casts = new HashMap<>();
        sizes = new HashMap<>();

        addCasts(JVMWeacTypes.BOOLEAN_TYPE, JVMWeacTypes.BYTE_TYPE, JVMWeacTypes.CHAR_TYPE, JVMWeacTypes.DOUBLE_TYPE, JVMWeacTypes.FLOAT_TYPE, JVMWeacTypes.INTEGER_TYPE, JVMWeacTypes.LONG_TYPE, JVMWeacTypes.SHORT_TYPE);
        addCasts(JVMWeacTypes.BYTE_TYPE, JVMWeacTypes.CHAR_TYPE, JVMWeacTypes.DOUBLE_TYPE, JVMWeacTypes.FLOAT_TYPE, JVMWeacTypes.INTEGER_TYPE, JVMWeacTypes.LONG_TYPE, JVMWeacTypes.SHORT_TYPE);
        addCasts(JVMWeacTypes.CHAR_TYPE, JVMWeacTypes.DOUBLE_TYPE, JVMWeacTypes.FLOAT_TYPE, JVMWeacTypes.INTEGER_TYPE, JVMWeacTypes.LONG_TYPE, JVMWeacTypes.SHORT_TYPE);
        addCasts(JVMWeacTypes.DOUBLE_TYPE);
        addCasts(JVMWeacTypes.FLOAT_TYPE, JVMWeacTypes.DOUBLE_TYPE);
        addCasts(JVMWeacTypes.INTEGER_TYPE, JVMWeacTypes.LONG_TYPE, JVMWeacTypes.DOUBLE_TYPE, JVMWeacTypes.FLOAT_TYPE);
        addCasts(JVMWeacTypes.LONG_TYPE, JVMWeacTypes.DOUBLE_TYPE);
        addCasts(JVMWeacTypes.SHORT_TYPE, JVMWeacTypes.CHAR_TYPE, JVMWeacTypes.DOUBLE_TYPE, JVMWeacTypes.FLOAT_TYPE, JVMWeacTypes.INTEGER_TYPE, JVMWeacTypes.LONG_TYPE);

        setSize(JVMWeacTypes.BOOLEAN_TYPE, 0);
        setSize(JVMWeacTypes.BYTE_TYPE, 1);
        setSize(JVMWeacTypes.SHORT_TYPE, 2);
        setSize(JVMWeacTypes.CHAR_TYPE, 2);
        setSize(JVMWeacTypes.INTEGER_TYPE, 3);
        setSize(JVMWeacTypes.FLOAT_TYPE, 4);
        setSize(JVMWeacTypes.LONG_TYPE, 5);
        setSize(JVMWeacTypes.DOUBLE_TYPE, 6);
    }

    private void setSize(WeacType type, int size) {
        sizes.put(type, size);
    }

    private void addCasts(WeacType from, WeacType... to) {
        List<WeacType> possible = new ArrayList<>();
        possible.add(JVMWeacTypes.OBJECT_TYPE);
        possible.add(JVMWeacTypes.JOBJECT_TYPE);
        possible.add(JVMWeacTypes.PRIMITIVE_TYPE);
        Collections.addAll(possible, to);
        casts.put(from, possible);
    }

    public boolean isPrimitiveCast(WeacType from, WeacType to) {
        return (from.getSuperType() != null && to.getSuperType() != null)
                && from.getSuperType().equals(JVMWeacTypes.PRIMITIVE_TYPE) && to.getSuperType().equals(JVMWeacTypes.PRIMITIVE_TYPE);
    }

    public boolean isCastable(WeacType from, WeacType to) {
        if(from.equals(to))
            return true;
        if(casts.containsKey(from)) {
            List<WeacType> possibleCasts = casts.get(from);
            for(WeacType t : possibleCasts) {
                if(t.equals(to)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int size(WeacType type) {
        return sizes.get(type);
    }
}
