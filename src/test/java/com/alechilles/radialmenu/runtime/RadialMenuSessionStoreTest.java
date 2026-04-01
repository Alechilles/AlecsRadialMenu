package com.alechilles.radialmenu.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class RadialMenuSessionStoreTest {
    @Test
    void storesStatePerPlayerAndMenuKey() {
        RadialMenuSessionStore store = new RadialMenuSessionStore();
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();

        store.setSelectedOptionId(playerA, "menu/a", "one");
        store.setSelectedOptionId(playerA, "menu/b", "two");
        store.setSelectedOptionId(playerB, "menu/a", "three");

        assertEquals("one", store.getSelectedOptionId(playerA, "menu/a"));
        assertEquals("two", store.getSelectedOptionId(playerA, "menu/b"));
        assertEquals("three", store.getSelectedOptionId(playerB, "menu/a"));
        assertNull(store.getSelectedOptionId(playerB, "menu/b"));
    }

    @Test
    void clearingPlayerOnlyRemovesThatPlayersState() {
        RadialMenuSessionStore store = new RadialMenuSessionStore();
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();

        store.setSelectedOptionId(playerA, "menu/a", "one");
        store.setSelectedOptionId(playerB, "menu/a", "two");

        store.clearPlayer(playerA);

        assertNull(store.getSelectedOptionId(playerA, "menu/a"));
        assertEquals("two", store.getSelectedOptionId(playerB, "menu/a"));
    }
}
