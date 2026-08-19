package com.alechilles.radialmenu.npc;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alechilles.radialmenu.api.RadialMenuNpcTarget;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.reference.PersistentRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

/** Keeps only a persistent NPC identity between page open and page selection. */
public final class RadialMenuNpcTargetHandle {
    private final PersistentRef persistentRef;
    private final ComponentType<EntityStore, NPCEntity> npcType;

    RadialMenuNpcTargetHandle(@Nonnull UUID npcUuid,
                              @Nonnull Ref<EntityStore> npcRef,
                              @Nonnull ComponentType<EntityStore, NPCEntity> npcType) {
        this.persistentRef = new PersistentRef();
        this.persistentRef.setEntity(npcRef, npcUuid);
        this.npcType = npcType;
    }

    @Nullable
    public static RadialMenuNpcTargetHandle capture(@Nullable Ref<EntityStore> npcRef,
                                                    @Nullable ComponentAccessor<EntityStore> accessor) {
        ComponentType<EntityStore, NPCEntity> npcType = NPCEntity.getComponentType();
        if (npcRef == null || !npcRef.isValid() || accessor == null || npcType == null) {
            return null;
        }
        NPCEntity npc = accessor.getComponent(npcRef, npcType);
        if (npc == null) {
            return null;
        }
        PersistentRef persistentRef = new PersistentRef();
        try {
            persistentRef.setEntity(npcRef, accessor);
        } catch (RuntimeException ex) {
            return null;
        }
        UUID npcUuid = persistentRef.getUuid();
        return npcUuid == null ? null : new RadialMenuNpcTargetHandle(npcUuid, npcRef, npcType);
    }

    @Nullable
    public RadialMenuNpcTarget resolve(@Nullable Store<EntityStore> store) {
        if (store == null || !persistentRef.isValid()) {
            return null;
        }
        try {
            Ref<EntityStore> npcRef = persistentRef.getEntity(store);
            if (npcRef == null || !npcRef.isValid()) {
                return null;
            }
            NPCEntity npc = store.getComponent(npcRef, npcType);
            UUID npcUuid = persistentRef.getUuid();
            if (npc == null || npcUuid == null) {
                return null;
            }
            return new RadialMenuNpcTarget(npcUuid, npcRef, npc, store);
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
