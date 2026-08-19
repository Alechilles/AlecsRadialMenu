package com.alechilles.radialmenu.api;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public interface RadialMenuApi {
    @Nonnull
    AutoCloseable registerActionHandler(@Nonnull String actionId, @Nonnull RadialMenuActionHandler handler);

    @Nonnull
    default AutoCloseable registerNpcActionHandler(@Nonnull String actionId,
                                                   @Nonnull RadialMenuNpcActionHandler handler) {
        throw new UnsupportedOperationException("NPC radial action handlers are not supported by this provider.");
    }

    boolean openMenu(@Nullable Player player, @Nullable String menuKey);

    default boolean openNpcMenu(@Nullable Player player,
                                @Nullable String menuKey,
                                @Nullable Ref<EntityStore> npcRef,
                                @Nullable ComponentAccessor<EntityStore> accessor) {
        return false;
    }

    boolean executeSelected(@Nullable Player player, @Nullable String menuKey);

    @Nonnull
    Set<String> listActionIds();

    @Nonnull
    default Set<String> listNpcActionIds() {
        return Set.of();
    }

    @Nonnull
    Set<String> listMenuKeys();
}
