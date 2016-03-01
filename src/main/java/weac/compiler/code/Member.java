package weac.compiler.code;

import weac.compiler.utils.ModifierType;
import weac.compiler.utils.WeacType;

import java.util.List;

public interface Member {
    String getName();

    String getCanonicalName();

    ModifierType getAccess();

    List<WeacType> getGenericParameterNames();
}
