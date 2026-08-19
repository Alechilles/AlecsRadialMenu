package com.alechilles.radialmenu.npc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

final class RadialMenuResultComponentTest {
    private static final long TTL_MILLIS = 5_000L;

    @Test
    void tracksOneResultPerPlayerWithoutCrossPlayerConsumption() {
        RadialMenuResultComponent component = new RadialMenuResultComponent();
        UUID firstPlayer = UUID.randomUUID();
        UUID secondPlayer = UUID.randomUUID();

        component.put(firstPlayer, "Companion", "Follow", 1_000L, TTL_MILLIS);
        component.put(secondPlayer, "Companion", "Stay", 1_100L, TTL_MILLIS);
        component.put(firstPlayer, "Companion", "Release", 1_200L, TTL_MILLIS);

        assertFalse(component.consume(firstPlayer, "companion", "follow", 1_300L));
        assertFalse(component.consume(secondPlayer, "companion", "release", 1_300L));
        assertTrue(component.consume(firstPlayer, "COMPANION", "RELEASE", 1_300L));
        assertTrue(component.consume(secondPlayer, "companion", "stay", 1_300L));
    }

    @Test
    void consumesAMatchingResultOnlyOnce() {
        RadialMenuResultComponent component = new RadialMenuResultComponent();
        UUID playerUuid = UUID.randomUUID();

        component.put(playerUuid, "companion", "follow", 10_000L, TTL_MILLIS);

        assertTrue(component.consume(playerUuid, "companion", "follow", 10_001L));
        assertFalse(component.consume(playerUuid, "companion", "follow", 10_002L));
    }

    @Test
    void expiredResultsDoNotMatch() {
        RadialMenuResultComponent component = new RadialMenuResultComponent();
        UUID playerUuid = UUID.randomUUID();

        component.put(playerUuid, "companion", "follow", 20_000L, TTL_MILLIS);

        assertFalse(component.consume(playerUuid, "companion", "follow", 25_000L));
    }
}
