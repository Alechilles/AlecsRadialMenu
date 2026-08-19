package com.alechilles.radialmenu.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.EmptyResourceStorage;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

final class RadialMenuNpcTargetHandleTest {
    @Test
    void resolvesOnlyWhileTheCapturedNpcStillExists() {
        ComponentRegistry<EntityStore> registry = new ComponentRegistry<>();
        var npcType = registry.registerComponent(NPCEntity.class, NPCEntity::new);
        var store = registry.addStore(null, EmptyResourceStorage.get());
        var npcRef = store.addEntity(Archetype.of(npcType), AddReason.SPAWN);
        UUID npcUuid = UUID.randomUUID();
        RadialMenuNpcTargetHandle handle = new RadialMenuNpcTargetHandle(npcUuid, npcRef, npcType);

        try {
            var resolved = handle.resolve(store);

            assertNotNull(resolved);
            assertEquals(npcUuid, resolved.npcUuid());
            assertSame(npcRef, resolved.reference());
            assertSame(store.getComponent(npcRef, npcType), resolved.npc());

            store.removeEntity(npcRef, RemoveReason.REMOVE);
            assertNull(handle.resolve(store));
        } finally {
            store.shutdown();
            registry.shutdown();
        }
    }
}
