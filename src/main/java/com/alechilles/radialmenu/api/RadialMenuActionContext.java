package com.alechilles.radialmenu.api;

import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.entity.entities.Player;

public record RadialMenuActionContext(@Nonnull Player player,
                                      @Nonnull String menuKey,
                                      @Nonnull String optionId,
                                      @Nonnull String actionId,
                                      @Nonnull String source,
                                      @Nonnull Map<String, String> payload) {
    public RadialMenuActionContext {
        Objects.requireNonNull(player, "player");
        menuKey = Objects.requireNonNull(menuKey, "menuKey");
        optionId = Objects.requireNonNull(optionId, "optionId");
        actionId = Objects.requireNonNull(actionId, "actionId");
        source = Objects.requireNonNull(source, "source");
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
}
