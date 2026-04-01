package com.alechilles.radialmenu.api.internal;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alechilles.radialmenu.api.RadialMenuActionHandler;
import com.alechilles.radialmenu.api.RadialMenuApi;
import com.alechilles.radialmenu.runtime.RadialMenuActionRegistry;
import com.alechilles.radialmenu.runtime.RadialMenuCatalog;
import com.alechilles.radialmenu.runtime.RadialMenuRuntimeService;
import com.hypixel.hytale.server.core.entity.entities.Player;

public final class RadialMenuApiImpl implements RadialMenuApi {
    private final RadialMenuActionRegistry actionRegistry;
    private final RadialMenuCatalog menuCatalog;
    private final RadialMenuRuntimeService runtimeService;

    public RadialMenuApiImpl(@Nonnull RadialMenuActionRegistry actionRegistry,
                             @Nonnull RadialMenuCatalog menuCatalog,
                             @Nonnull RadialMenuRuntimeService runtimeService) {
        this.actionRegistry = actionRegistry;
        this.menuCatalog = menuCatalog;
        this.runtimeService = runtimeService;
    }

    @Nonnull
    @Override
    public AutoCloseable registerActionHandler(@Nonnull String actionId, @Nonnull RadialMenuActionHandler handler) {
        return actionRegistry.register(actionId, handler);
    }

    @Override
    public boolean openMenu(@Nullable Player player, @Nullable String menuKey) {
        return runtimeService.openMenu(player, menuKey, null, "api");
    }

    @Override
    public boolean executeSelected(@Nullable Player player, @Nullable String menuKey) {
        return runtimeService.executeSelected(player, menuKey, null, "api");
    }

    @Nonnull
    @Override
    public Set<String> listActionIds() {
        return actionRegistry.listActionIds();
    }

    @Nonnull
    @Override
    public Set<String> listMenuKeys() {
        return menuCatalog.listMenuKeys();
    }
}
