package weac.compiler.code;

import weac.compiler.utils.ModifierType;

import java.util.List;

public interface Member {
    String getName();

    String getCanonicalName();

    ModifierType getAccess();

    List<String> getGenericParameterNames();
}
