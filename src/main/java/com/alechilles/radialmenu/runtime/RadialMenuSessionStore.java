package com.alechilles.radialmenu.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

public final class RadialMenuSessionStore {
    private final ConcurrentHashMap<UUID, Map<String, String>> selectedByPlayerAndMenu = new ConcurrentHashMap<>();

    @Nullable
    public String getSelectedOptionId(@Nullable UUID playerUuid, @Nullable String menuKey) {
        String normalizedMenuKey = RadialMenuCatalog.normalizeKey(menuKey);
        if (playerUuid == null || normalizedMenuKey == null) {
            return null;
        }
        Map<String, String> byMenu = selectedByPlayerAndMenu.get(playerUuid);
        if (byMenu == null) {
            return null;
        }
        return byMenu.get(normalizedMenuKey);
    }

    public void setSelectedOptionId(@Nullable UUID playerUuid, @Nullable String menuKey, @Nullable String optionId) {
        String normalizedMenuKey = RadialMenuCatalog.normalizeKey(menuKey);
        String normalizedOptionId = RadialMenuCatalog.normalizeKey(optionId);
        if (playerUuid == null || normalizedMenuKey == null || normalizedOptionId == null) {
            return;
        }
        selectedByPlayerAndMenu.compute(playerUuid, (uuid, existing) -> {
            Map<String, String> byMenu = existing == null ? new HashMap<>() : new HashMap<>(existing);
            byMenu.put(normalizedMenuKey, normalizedOptionId);
            return byMenu;
        });
    }

    public void clearPlayer(@Nullable UUID playerUuid) {
        if (playerUuid == null) {
            return;
        }
        selectedByPlayerAndMenu.remove(playerUuid);
    }

    public void clearAll() {
        selectedByPlayerAndMenu.clear();
    }
}
