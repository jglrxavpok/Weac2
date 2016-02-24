package weac.compiler.resolve;

import weac.compiler.CompileUtils;
import weac.compiler.utils.WeacType;

import java.util.*;

public class PrimitiveCastSolver extends CompileUtils {

    private Map<WeacType, List<WeacType>> casts;

    public PrimitiveCastSolver() {
        casts = new HashMap<>();

        addCasts(WeacType.BOOLEAN_TYPE, WeacType.BYTE_TYPE, WeacType.CHAR_TYPE, WeacType.DOUBLE_TYPE, WeacType.FLOAT_TYPE, WeacType.INTEGER_TYPE, WeacType.LONG_TYPE, WeacType.SHORT_TYPE);
        addCasts(WeacType.BYTE_TYPE, WeacType.CHAR_TYPE, WeacType.DOUBLE_TYPE, WeacType.FLOAT_TYPE, WeacType.INTEGER_TYPE, WeacType.LONG_TYPE, WeacType.SHORT_TYPE);
        addCasts(WeacType.CHAR_TYPE, WeacType.DOUBLE_TYPE, WeacType.FLOAT_TYPE, WeacType.INTEGER_TYPE, WeacType.LONG_TYPE, WeacType.SHORT_TYPE);
        addCasts(WeacType.DOUBLE_TYPE);
        addCasts(WeacType.FLOAT_TYPE, WeacType.DOUBLE_TYPE);
        addCasts(WeacType.INTEGER_TYPE, WeacType.LONG_TYPE);
        addCasts(WeacType.LONG_TYPE, WeacType.DOUBLE_TYPE);
        addCasts(WeacType.SHORT_TYPE, WeacType.CHAR_TYPE, WeacType.DOUBLE_TYPE, WeacType.FLOAT_TYPE, WeacType.INTEGER_TYPE, WeacType.LONG_TYPE);
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
}
