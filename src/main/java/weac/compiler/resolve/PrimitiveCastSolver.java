package weac.compiler.resolve;

import weac.compiler.CompileUtils;
import weac.compiler.utils.WeacType;

import java.util.*;

public class PrimitiveCastSolver extends CompileUtils {

    private Map<WeacType, List<WeacType>> casts;
    private Map<WeacType, Integer> sizes;

    public PrimitiveCastSolver() {
        casts = new HashMap<>();
        sizes = new HashMap<>();

        addCasts(WeacType.BOOLEAN_TYPE, WeacType.BYTE_TYPE, WeacType.CHAR_TYPE, WeacType.DOUBLE_TYPE, WeacType.FLOAT_TYPE, WeacType.INTEGER_TYPE, WeacType.LONG_TYPE, WeacType.SHORT_TYPE);
        addCasts(WeacType.BYTE_TYPE, WeacType.CHAR_TYPE, WeacType.DOUBLE_TYPE, WeacType.FLOAT_TYPE, WeacType.INTEGER_TYPE, WeacType.LONG_TYPE, WeacType.SHORT_TYPE);
        addCasts(WeacType.CHAR_TYPE, WeacType.DOUBLE_TYPE, WeacType.FLOAT_TYPE, WeacType.INTEGER_TYPE, WeacType.LONG_TYPE, WeacType.SHORT_TYPE);
        addCasts(WeacType.DOUBLE_TYPE);
        addCasts(WeacType.FLOAT_TYPE, WeacType.DOUBLE_TYPE);
        addCasts(WeacType.INTEGER_TYPE, WeacType.LONG_TYPE, WeacType.DOUBLE_TYPE, WeacType.FLOAT_TYPE);
        addCasts(WeacType.LONG_TYPE, WeacType.DOUBLE_TYPE);
        addCasts(WeacType.SHORT_TYPE, WeacType.CHAR_TYPE, WeacType.DOUBLE_TYPE, WeacType.FLOAT_TYPE, WeacType.INTEGER_TYPE, WeacType.LONG_TYPE);

        setSize(WeacType.BOOLEAN_TYPE, 0);
        setSize(WeacType.BYTE_TYPE, 1);
        setSize(WeacType.SHORT_TYPE, 2);
        setSize(WeacType.CHAR_TYPE, 2);
        setSize(WeacType.INTEGER_TYPE, 3);
        setSize(WeacType.FLOAT_TYPE, 4);
        setSize(WeacType.LONG_TYPE, 5);
        setSize(WeacType.DOUBLE_TYPE, 6);
    }

    private void setSize(WeacType type, int size) {
        sizes.put(type, size);
    }

    private void addCasts(WeacType from, WeacType... to) {
        List<WeacType> possible = new ArrayList<>();
        possible.add(WeacType.OBJECT_TYPE);
        possible.add(WeacType.JOBJECT_TYPE);
        possible.add(WeacType.PRIMITIVE_TYPE);
        Collections.addAll(possible, to);
        casts.put(from, possible);
    }

    public boolean isPrimitiveCast(WeacType from, WeacType to) {
        return (from.getSuperType() != null && to.getSuperType() != null)
                && from.getSuperType().equals(WeacType.PRIMITIVE_TYPE) && to.getSuperType().equals(WeacType.PRIMITIVE_TYPE);
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
