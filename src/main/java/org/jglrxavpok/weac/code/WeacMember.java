package org.jglrxavpok.weac.code;

import org.jglrxavpok.weac.utils.WeacModifierType;

public interface WeacMember {
    String getName();

    String getCanonicalName();

    WeacModifierType getAccess();
}
