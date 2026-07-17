package com.alechilles.radialmenu.runtime;

import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alechilles.radialmenu.api.RadialMenuActionContext;
import com.alechilles.radialmenu.api.RadialMenuActionHandler;
import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecuteCommandOption;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecutionMode;
import com.alechilles.radialmenu.config.RadialMenuConfig.Feedback;
import com.alechilles.radialmenu.config.RadialMenuConfig.InvokeRegisteredActionOption;
import com.alechilles.radialmenu.config.RadialMenuConfig.Option;
import com.alechilles.radialmenu.config.RadialMenuConfig.RunInteractionOption;
import com.alechilles.radialmenu.localization.RadialMenuLocalizedText;
import com.alechilles.radialmenu.ui.RadialMenuPage;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class RadialMenuRuntimeService {
    private final RadialMenuCatalog catalog;
    private final RadialMenuSessionStore sessions;
    private final RadialMenuActionRegistry actionRegistry;
    private final PlayerCommandDispatcher commandDispatcher;
    private final RadialMenuInteractionRunner interactionRunner;
    @Nullable
    private final HytaleLogger logger;

    public RadialMenuRuntimeService(@Nonnull RadialMenuCatalog catalog,
                                    @Nonnull RadialMenuSessionStore sessions,
                                    @Nonnull RadialMenuActionRegistry actionRegistry,
                                    @Nonnull PlayerCommandDispatcher commandDispatcher,
                                    @Nullable HytaleLogger logger) {
        this(catalog, sessions, actionRegistry, commandDispatcher, new HytaleInteractionRunner(logger), logger);
    }

    RadialMenuRuntimeService(@Nonnull RadialMenuCatalog catalog,
                             @Nonnull RadialMenuSessionStore sessions,
                             @Nonnull RadialMenuActionRegistry actionRegistry,
                             @Nonnull PlayerCommandDispatcher commandDispatcher,
                             @Nonnull RadialMenuInteractionRunner interactionRunner,
                             @Nullable HytaleLogger logger) {
        this.catalog = catalog;
        this.sessions = sessions;
        this.actionRegistry = actionRegistry;
        this.commandDispatcher = commandDispatcher;
        this.interactionRunner = interactionRunner;
        this.logger = logger;
    }

    @Nullable
    public String resolveMenuKey(@Nullable String menuKeyOverride, @Nullable String itemId) {
        refreshCatalogFromAssetStore();
        String normalizedOverride = RadialMenuCatalog.normalizeKey(menuKeyOverride);
        if (normalizedOverride != null) {
            return normalizedOverride;
        }
        return catalog.resolveMenuKeyForItem(itemId);
    }

    public boolean openMenu(@Nullable Player player,
                            @Nullable String menuKey,
                            @Nullable ExecutionMode modeOverride,
                            @Nonnull String source) {
        refreshCatalogFromAssetStore();
        String normalizedMenuKey = RadialMenuCatalog.normalizeKey(menuKey);
        if (player == null) {
            return false;
        }
        if (normalizedMenuKey == null) {
            warnAndLog(player, "radialmenu.warning.menu.unavailable", "No menu key resolved for open operation.", "<none>");
            return false;
        }
        RadialMenuConfig menu = catalog.getByMenuKey(normalizedMenuKey);
        if (menu == null) {
            warnAndLog(player, "radialmenu.warning.menu.unavailable", "Unknown menu key: " + normalizedMenuKey, normalizedMenuKey);
            return false;
        }
        Option[] options = menu.getOptions();
        if (options == null || options.length == 0) {
            warnAndLog(player, "radialmenu.warning.selection.unavailable", "Menu has no options: " + normalizedMenuKey);
            return false;
        }

        World world = player.getWorld();
        Ref<EntityStore> playerRef = player.getReference();
        PlayerRef uiPlayerRef = player.getPlayerRef();
        if (world == null || playerRef == null || !playerRef.isValid() || uiPlayerRef == null || !uiPlayerRef.isValid()) {
            warnAndLog(player, "radialmenu.warning.open.unavailable", "Player world/page context unavailable for menu: " + normalizedMenuKey);
            return false;
        }
        if (player.getPageManager() == null) {
            warnAndLog(player, "radialmenu.warning.open.unavailable", "Player page manager unavailable for menu: " + normalizedMenuKey);
            return false;
        }
        Store<EntityStore> store = world.getEntityStore().getStore();
        if (store == null) {
            warnAndLog(player, "radialmenu.warning.open.unavailable", "Entity store unavailable for menu: " + normalizedMenuKey);
            return false;
        }

        String selectedOptionId = sessions.getSelectedOptionId(player.getUuid(), normalizedMenuKey);
        RadialMenuPage page = new RadialMenuPage(
                uiPlayerRef,
                normalizedMenuKey,
                menu,
                selectedOptionId,
                optionId -> selectOption(player, normalizedMenuKey, optionId, modeOverride, source + ".menu"),
                logger
        );
        player.getPageManager().openCustomPage(playerRef, store, page);
        return true;
    }

    public boolean selectOption(@Nullable Player player,
                                @Nullable String menuKey,
                                @Nullable String optionId,
                                @Nullable ExecutionMode modeOverride,
                                @Nonnull String source) {
        String normalizedMenuKey = RadialMenuCatalog.normalizeKey(menuKey);
        if (player == null) {
            return false;
        }
        if (normalizedMenuKey == null) {
            warnAndLog(player, "radialmenu.warning.menu.unavailable", "No menu key resolved for selectOption.", "<none>");
            return false;
        }
        return handleOptionSelection(player, normalizedMenuKey, optionId, modeOverride, source);
    }

    public boolean executeSelected(@Nullable Player player,
                                   @Nullable String menuKey,
                                   @Nullable ExecutionMode modeOverride,
                                   @Nonnull String source) {
        return executeSelected(player, menuKey, modeOverride, source, null);
    }

    public boolean executeSelected(@Nullable Player player,
                                   @Nullable String menuKey,
                                   @Nullable ExecutionMode modeOverride,
                                   @Nonnull String source,
                                   @Nullable InteractionContext activeContext) {
        refreshCatalogFromAssetStore();
        String normalizedMenuKey = RadialMenuCatalog.normalizeKey(menuKey);
        if (player == null) {
            return false;
        }
        if (normalizedMenuKey == null) {
            warnAndLog(player, "radialmenu.warning.menu.unavailable", "No menu key resolved for executeSelected.", "<none>");
            return false;
        }
        RadialMenuConfig menu = catalog.getByMenuKey(normalizedMenuKey);
        if (menu == null) {
            warnAndLog(player, "radialmenu.warning.menu.unavailable", "Unknown menu key: " + normalizedMenuKey, normalizedMenuKey);
            return false;
        }

        String selectedId = sessions.getSelectedOptionId(player.getUuid(), normalizedMenuKey);
        Option selected = menu.findOptionById(selectedId);
        if (selected == null) {
            selected = menu.resolveDefaultOption();
        }
        if (selected == null || selected.getId() == null || selected.getId().isBlank()) {
            warnAndLog(player, "radialmenu.warning.selection.unavailable", "No selected/default option for menu: " + normalizedMenuKey);
            return false;
        }

        sessions.setSelectedOptionId(player.getUuid(), normalizedMenuKey, selected.getId());
        return executeOption(
                player,
                normalizedMenuKey,
                menu,
                selected,
                modeOverride,
                source + ".execute",
                activeContext
        );
    }

    private boolean handleOptionSelection(@Nonnull Player player,
                                          @Nonnull String menuKey,
                                          @Nullable String optionId,
                                          @Nullable ExecutionMode modeOverride,
                                          @Nonnull String source) {
        RadialMenuConfig menu = catalog.getByMenuKey(menuKey);
        if (menu == null) {
            warnAndLog(player, "radialmenu.warning.menu.unavailable", "Unknown menu key: " + menuKey, menuKey);
            return false;
        }

        Option option = menu.findOptionById(optionId);
        if (option == null || option.getId() == null || option.getId().isBlank()) {
            warnAndLog(player, "radialmenu.warning.selection.unavailable", "Selected option unavailable for menu: " + menuKey);
            return false;
        }

        sessions.setSelectedOptionId(player.getUuid(), menuKey, option.getId());
        ExecutionMode mode = resolveMode(menu, modeOverride);
        if (mode == ExecutionMode.SelectAndArm) {
            sendInfo(player, "radialmenu.info.selection.selected", resolveOptionLabel(player, option));
            applyFeedback(player, option.getFeedback());
            return true;
        }

        return executeOption(player, menuKey, menu, option, modeOverride, source + ".selectAndRun", null);
    }

    private boolean executeOption(@Nonnull Player player,
                                  @Nonnull String menuKey,
                                  @Nonnull RadialMenuConfig menu,
                                  @Nonnull Option option,
                                  @Nullable ExecutionMode modeOverride,
                                  @Nonnull String source,
                                  @Nullable InteractionContext activeContext) {
        boolean executed;

        if (option instanceof ExecuteCommandOption executeCommandOption) {
            String command = executeCommandOption.getCommand();
            if (command == null || command.isBlank()) {
                warnAndLog(player, "radialmenu.warning.execute.commandBlank", "Blank ExecuteCommand payload on menu: " + menuKey);
                return false;
            }
            executed = commandDispatcher.dispatch(player, command);
            if (!executed && command.startsWith("/")) {
                executed = commandDispatcher.dispatch(player, command.substring(1));
            }
            if (!executed) {
                warnAndLog(player, "radialmenu.warning.execute.commandFailed", "Command dispatch failed: " + command, command);
                return false;
            }
        } else if (option instanceof InvokeRegisteredActionOption invokeRegisteredActionOption) {
            String actionId = invokeRegisteredActionOption.getActionId();
            if (actionId == null || actionId.isBlank()) {
                warnAndLog(player, "radialmenu.warning.execute.actionBlank", "Blank ActionId on menu: " + menuKey);
                return false;
            }
            RadialMenuActionHandler handler = actionRegistry.get(actionId);
            if (handler == null) {
                warnAndLog(player, "radialmenu.warning.execute.handlerMissing", "No handler for actionId: " + actionId, actionId);
                return false;
            }
            String optionId = option.getId() == null ? "unknown" : option.getId();
            Map<String, String> payload = invokeRegisteredActionOption.getPayload();
            RadialMenuActionContext context = new RadialMenuActionContext(
                    player,
                    menuKey,
                    optionId,
                    actionId,
                    source,
                    payload
            );
            try {
                executed = handler.handle(context);
            } catch (Throwable ex) {
                if (logger != null) {
                    logger.at(Level.WARNING).withCause(ex).log(
                            "RadialMenu action handler threw for actionId '" + actionId + "' on menu '" + menuKey + "'."
                    );
                }
                warn(player, "radialmenu.warning.execute.handlerFailed", actionId);
                return false;
            }
            if (!executed) {
                warnAndLog(player, "radialmenu.warning.execute.handlerFailed", "Action handler returned false: " + actionId, actionId);
                return false;
            }
        } else if (option instanceof RunInteractionOption runInteractionOption) {
            String rootInteraction = runInteractionOption.getRootInteraction();
            if (rootInteraction == null || rootInteraction.isBlank()) {
                warnAndLog(
                        player,
                        "radialmenu.warning.execute.interactionBlank",
                        "Blank RootInteraction on menu: " + menuKey
                );
                return false;
            }
            try {
                executed = interactionRunner.run(player, runInteractionOption, activeContext);
            } catch (Throwable ex) {
                if (logger != null) {
                    logger.at(Level.WARNING).withCause(ex).log(
                            "RadialMenu interaction runner threw for root '" + rootInteraction
                                    + "' on menu '" + menuKey + "'."
                    );
                }
                warn(player, "radialmenu.warning.execute.interactionFailed", rootInteraction);
                return false;
            }
            if (!executed) {
                warnAndLog(
                        player,
                        "radialmenu.warning.execute.interactionFailed",
                        "RootInteraction execution failed: " + rootInteraction,
                        rootInteraction
                );
                return false;
            }
        } else {
            warnAndLog(player, "radialmenu.warning.execute.unsupported", "Unsupported option type for menu: " + menuKey);
            return false;
        }

        sendInfo(player, "radialmenu.info.execute.success", resolveOptionLabel(player, option));
        applyFeedback(player, option.getFeedback());
        if (logger != null && modeOverride != null && modeOverride != menu.getExecutionMode()) {
            logger.at(Level.FINER).log("RadialMenu mode override used for menu '" + menuKey + "': " + modeOverride.name());
        }
        return true;
    }

    private void applyFeedback(@Nonnull Player player, @Nullable Feedback feedback) {
        if (feedback == null) {
            return;
        }
        if (feedback.getChatMessage() != null && !feedback.getChatMessage().isBlank()) {
            sendRaw(player, feedback.getChatMessage());
        }
        if (feedback.getHudMessage() != null && !feedback.getHudMessage().isBlank()) {
            sendRaw(player, feedback.getHudMessage());
        }
        if ((feedback.getSoundEvent() != null && !feedback.getSoundEvent().isBlank())
                || (feedback.getParticleSystem() != null && !feedback.getParticleSystem().isBlank())) {
            if (logger != null) {
                logger.at(Level.FINER).log(
                        "RadialMenu feedback hint ignored (sound/particle unsupported in v1 runtime)."
                );
            }
        }
    }

    @Nonnull
    private ExecutionMode resolveMode(@Nonnull RadialMenuConfig menu, @Nullable ExecutionMode modeOverride) {
        return modeOverride != null ? modeOverride : menu.getExecutionMode();
    }

    @Nonnull
    private String resolveOptionLabel(@Nonnull Player player, @Nonnull Option option) {
        if (option.getLabel() != null && !option.getLabel().isBlank()) {
            return option.getLabel();
        }
        if (option.getLabelKey() != null && !option.getLabelKey().isBlank()) {
            return RadialMenuLocalizedText.resolve(player, option.getLabelKey());
        }
        if (option.getId() != null && !option.getId().isBlank()) {
            return option.getId();
        }
        return RadialMenuLocalizedText.resolve(player, "radialmenu.ui.unknownOption");
    }

    private void warnAndLog(@Nonnull Player player,
                            @Nonnull String translationKey,
                            @Nonnull String logMessage,
                            Object... args) {
        warn(player, translationKey, args);
        if (logger != null) {
            logger.at(Level.WARNING).log("RadialMenu: " + logMessage);
        }
    }

    private void warn(@Nonnull Player player, @Nonnull String translationKey, Object... args) {
        sendRaw(player, RadialMenuLocalizedText.format(player, translationKey, args));
    }

    private void sendInfo(@Nonnull Player player, @Nonnull String translationKey, Object... args) {
        sendRaw(player, RadialMenuLocalizedText.format(player, translationKey, args));
    }

    private void sendRaw(@Nonnull Player player, @Nullable String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        PlayerRef playerRef = player.getPlayerRef();
        if (playerRef != null && playerRef.isValid()) {
            playerRef.sendMessage(Message.raw(text));
        }
    }

    private void refreshCatalogFromAssetStore() {
        DefaultAssetMap<String, RadialMenuConfig> map = RadialMenuConfig.getAssetMap();
        if (map == null || map.getAssetMap() == null) {
            return;
        }
        Map<String, RadialMenuConfig> assets = map.getAssetMap();
        catalog.rebuild(assets, logger);
    }
}
