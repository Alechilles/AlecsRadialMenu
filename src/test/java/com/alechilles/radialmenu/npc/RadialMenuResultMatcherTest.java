package com.alechilles.radialmenu.npc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.EmptyResourceStorage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

final class RadialMenuResultMatcherTest {
    @Test
    void consumesOnlyTheCurrentPlayersExactResult() {
        ComponentRegistry<EntityStore> registry = new ComponentRegistry<>();
        var resultType = registry.registerComponent(
                RadialMenuResultComponent.class,
                "RadialMenuResultTest",
                RadialMenuResultComponent.CODEC
        );
        var store = registry.addStore(null, EmptyResourceStorage.get());
        var npcRef = store.addEntity(Archetype.of(resultType), AddReason.SPAWN);
        UUID currentPlayer = UUID.randomUUID();
        UUID otherPlayer = UUID.randomUUID();
        RadialMenuResultComponent component = store.getComponent(npcRef, resultType);
        component.put(currentPlayer, "companion", "follow", 1_000L, 5_000L);
        component.put(otherPlayer, "companion", "stay", 1_000L, 5_000L);

        try {
            assertFalse(RadialMenuResultMatcher.consume(
                    store,
                    npcRef,
                    resultType,
                    otherPlayer,
                    "companion",
                    "follow",
                    1_100L
            ));
            assertTrue(RadialMenuResultMatcher.consume(
                    store,
                    npcRef,
                    resultType,
                    currentPlayer,
                    "COMPANION",
                    "FOLLOW",
                    1_100L
            ));
            assertFalse(RadialMenuResultMatcher.consume(
                    store,
                    npcRef,
                    resultType,
                    currentPlayer,
                    "companion",
                    "follow",
                    1_100L
            ));
            assertTrue(RadialMenuResultMatcher.consume(
                    store,
                    npcRef,
                    resultType,
                    otherPlayer,
                    "companion",
                    "stay",
                    1_100L
            ));
        } finally {
            store.shutdown();
            registry.shutdown();
        }
    }
}
