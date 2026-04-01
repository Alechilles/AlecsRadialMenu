package com.alechilles.radialmenu.api;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.server.core.entity.entities.Player;

public interface RadialMenuApi {
    @Nonnull
    AutoCloseable registerActionHandler(@Nonnull String actionId, @Nonnull RadialMenuActionHandler handler);

    boolean openMenu(@Nullable Player player, @Nullable String menuKey);

    boolean executeSelected(@Nullable Player player, @Nullable String menuKey);

    @Nonnull
    Set<String> listActionIds();

    @Nonnull
    Set<String> listMenuKeys();
}
