package com.alechilles.radialmenu.runtime;

import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alechilles.radialmenu.config.RadialMenuConfig.RunInteractionOption;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

final class HytaleInteractionRunner implements RadialMenuInteractionRunner {
    @Nullable
    private final HytaleLogger logger;

    HytaleInteractionRunner(@Nullable HytaleLogger logger) {
        this.logger = logger;
    }

    @Override
    public boolean run(@Nonnull Player player,
                       @Nonnull RunInteractionOption option,
                       @Nullable InteractionContext activeContext) {
        String rootInteractionId = option.getRootInteraction();
        if (rootInteractionId == null || rootInteractionId.isBlank()) {
            return false;
        }

        RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset(rootInteractionId);
        if (rootInteraction == null) {
            logFailure("Unknown RootInteraction asset: " + rootInteractionId, null);
            return false;
        }

        try {
            if (activeContext != null) {
                activeContext.getState().state = InteractionState.Finished;
                activeContext.execute(rootInteraction);
                return true;
            }
            return runStandalone(player, option.getInteractionType(), rootInteraction);
        } catch (Throwable ex) {
            logFailure("Failed to run RootInteraction asset: " + rootInteractionId, ex);
            return false;
        }
    }

    private boolean runStandalone(@Nonnull Player player,
                                  @Nonnull InteractionType interactionType,
                                  @Nonnull RootInteraction rootInteraction) {
        Ref<EntityStore> playerRef = player.getReference();
        World world = player.getWorld();
        if (playerRef == null || !playerRef.isValid() || world == null) {
            return false;
        }

        Store<EntityStore> store = world.getEntityStore().getStore();
        if (store == null) {
            return false;
        }

        InteractionManager interactionManager = store.getComponent(
                playerRef,
                InteractionModule.get().getInteractionManagerComponent()
        );
        if (interactionManager == null || !interactionManager.canRun(interactionType, rootInteraction)) {
            return false;
        }

        InteractionContext interactionContext = InteractionContext.forInteraction(
                interactionManager,
                playerRef,
                interactionType,
                store
        );
        var chain = interactionManager.initChain(interactionType, interactionContext, rootInteraction, false);
        interactionManager.queueExecuteChain(chain);
        return true;
    }

    private void logFailure(@Nonnull String message, @Nullable Throwable throwable) {
        if (logger == null) {
            return;
        }
        if (throwable == null) {
            logger.at(Level.WARNING).log("RadialMenu: " + message);
            return;
        }
        logger.at(Level.WARNING).withCause(throwable).log("RadialMenu: " + message);
    }
}
