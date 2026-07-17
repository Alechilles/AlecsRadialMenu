package com.alechilles.radialmenu.runtime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alechilles.radialmenu.config.RadialMenuConfig.RunInteractionOption;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;

@FunctionalInterface
interface RadialMenuInteractionRunner {
    boolean run(@Nonnull Player player,
                @Nonnull RunInteractionOption option,
                @Nullable InteractionContext activeContext);
}
