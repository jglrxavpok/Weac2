package weac.compiler.code;

import weac.compiler.utils.WeacModifierType;

import java.util.List;

public interface WeacMember {
    String getName();

    String getCanonicalName();

    WeacModifierType getAccess();

    List<String> getGenericParameterNames();
}
