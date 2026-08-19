package com.alechilles.radialmenu.npc;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

final class RadialMenuResultMatcher {
    private RadialMenuResultMatcher() {
    }

    static boolean consume(@Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> npcRef,
                           @Nullable ComponentType<EntityStore, RadialMenuResultComponent> componentType,
                           @Nullable UUID playerUuid,
                           @Nullable String menuKey,
                           @Nullable String resultId,
                           long nowMillis) {
        if (!npcRef.isValid() || componentType == null || playerUuid == null) {
            return false;
        }
        RadialMenuResultComponent component = store.getComponent(npcRef, componentType);
        return component != null && component.consume(playerUuid, menuKey, resultId, nowMillis);
    }
}
