package com.alechilles.radialmenu.api;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

/**
 * A live NPC target for one radial action callback.
 *
 * <p>Handlers must not retain this target, its reference, its NPC component, or its store.</p>
 */
public record RadialMenuNpcTarget(@Nonnull UUID npcUuid,
                                  @Nonnull Ref<EntityStore> reference,
                                  @Nonnull NPCEntity npc,
                                  @Nonnull Store<EntityStore> store) {
    public RadialMenuNpcTarget {
        Objects.requireNonNull(npcUuid, "npcUuid");
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(npc, "npc");
        Objects.requireNonNull(store, "store");
    }
}
