package com.alechilles.radialmenu;

import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alechilles.radialmenu.api.RadialMenuApi;
import com.alechilles.radialmenu.api.internal.RadialMenuApiImpl;
import com.alechilles.radialmenu.assets.RadialMenuAssetPackCoordinator;
import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.interactions.RadialMenuInteraction;
import com.alechilles.radialmenu.metrics.RadialMenuHStatsIntegration;
import com.alechilles.radialmenu.runtime.PlayerCommandDispatcher;
import com.alechilles.radialmenu.runtime.RadialMenuActionRegistry;
import com.alechilles.radialmenu.runtime.RadialMenuCatalog;
import com.alechilles.radialmenu.runtime.RadialMenuRuntimeService;
import com.alechilles.radialmenu.runtime.RadialMenuSessionStore;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.event.RemovedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public final class RadialMenuMod extends JavaPlugin {
    private static RadialMenuMod instance;

    private RadialMenuCatalog menuCatalog;
    private RadialMenuSessionStore sessionStore;
    private RadialMenuActionRegistry actionRegistry;
    private RadialMenuRuntimeService runtimeService;
    private RadialMenuApi api;
    private RadialMenuAssetPackCoordinator assetPackCoordinator;
    private RadialMenuHStatsIntegration hStatsIntegration;
    private boolean menuAssetsRegistered;

    public RadialMenuMod(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        menuCatalog = new RadialMenuCatalog();
        sessionStore = new RadialMenuSessionStore();
        actionRegistry = new RadialMenuActionRegistry();

        PlayerCommandDispatcher commandDispatcher = new PlayerCommandDispatcher(this, getLogger());
        runtimeService = new RadialMenuRuntimeService(menuCatalog, sessionStore, actionRegistry, commandDispatcher, getLogger());
        api = new RadialMenuApiImpl(actionRegistry, menuCatalog, runtimeService);
        assetPackCoordinator = new RadialMenuAssetPackCoordinator(this);
        hStatsIntegration = new RadialMenuHStatsIntegration(this);

        Interaction.CODEC.register(
                "RadialMenuInteraction",
                RadialMenuInteraction.class,
                RadialMenuInteraction.CODEC
        );

        registerMenuAssets();
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
    }

    @Override
    protected void start() {
        refreshCatalog();
        if (assetPackCoordinator != null) {
            assetPackCoordinator.ensureAssetEditorPackVisible();
        }
        if (hStatsIntegration != null) {
            hStatsIntegration.initialize();
        }
        getLogger().at(java.util.logging.Level.INFO).log(
                "Alec's Radial Menu enabled. Menus loaded: " + menuCatalog.listMenuKeys().size()
        );
    }

    @Override
    protected void shutdown() {
        if (sessionStore != null) {
            sessionStore.clearAll();
        }
        instance = null;
        api = null;
        runtimeService = null;
        actionRegistry = null;
        menuCatalog = null;
        sessionStore = null;
        assetPackCoordinator = null;
        hStatsIntegration = null;
        getLogger().at(java.util.logging.Level.INFO).log("Alec's Radial Menu disabled.");
    }

    private void registerMenuAssets() {
        if (menuAssetsRegistered) {
            return;
        }
        getAssetRegistry().register(
                HytaleAssetStore.builder(RadialMenuConfig.class, new DefaultAssetMap<>())
                        .setPath("RadialMenu/Menus")
                        .setCodec(RadialMenuConfig.CODEC)
                        .setKeyFunction(RadialMenuConfig::getKey)
                        .build()
        );
        getEventRegistry().register(LoadedAssetsEvent.class, RadialMenuConfig.class, this::onMenuAssetsLoaded);
        getEventRegistry().register(RemovedAssetsEvent.class, RadialMenuConfig.class, this::onMenuAssetsRemoved);
        menuAssetsRegistered = true;
    }

    private void onMenuAssetsLoaded(
            LoadedAssetsEvent<String, RadialMenuConfig, DefaultAssetMap<String, RadialMenuConfig>> event) {
        refreshCatalog();
    }

    private void onMenuAssetsRemoved(
            RemovedAssetsEvent<String, RadialMenuConfig, DefaultAssetMap<String, RadialMenuConfig>> event) {
        refreshCatalog();
    }

    private void refreshCatalog() {
        if (menuCatalog == null) {
            return;
        }
        DefaultAssetMap<String, RadialMenuConfig> map = RadialMenuConfig.getAssetMap();
        Map<String, RadialMenuConfig> assets = map == null || map.getAssetMap() == null
                ? Collections.emptyMap()
                : map.getAssetMap();
        menuCatalog.rebuild(assets, getLogger());
    }

    private void onPlayerDisconnect(@Nonnull PlayerDisconnectEvent event) {
        if (sessionStore == null || event.getPlayerRef() == null) {
            return;
        }
        sessionStore.clearPlayer(event.getPlayerRef().getUuid());
    }

    @Nullable
    public static RadialMenuMod getInstance() {
        return instance;
    }

    @Nullable
    public static RadialMenuApi getApiInstance() {
        return instance != null ? instance.api : null;
    }

    @Nullable
    public RadialMenuApi getApi() {
        return api;
    }

    @Nullable
    public RadialMenuRuntimeService getRuntimeService() {
        return runtimeService;
    }
}
