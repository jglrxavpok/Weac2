package org.jglrxavpok.weac.code;

import org.jglrxavpok.weac.utils.WeacModifierType;

import java.util.List;

public interface WeacMember {
    String getName();

    String getCanonicalName();

    WeacModifierType getAccess();

    List<String> getGenericParameterNames();
}
