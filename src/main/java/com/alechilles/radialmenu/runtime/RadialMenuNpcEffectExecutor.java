package com.alechilles.radialmenu.runtime;

import java.util.UUID;

import com.alechilles.radialmenu.api.RadialMenuNpcTarget;

interface RadialMenuNpcEffectExecutor {
    boolean setState(RadialMenuNpcTarget target, String state, String subState);

    boolean emitResult(RadialMenuNpcTarget target,
                       UUID playerUuid,
                       String menuKey,
                       String resultId,
                       long nowMillis);
}
