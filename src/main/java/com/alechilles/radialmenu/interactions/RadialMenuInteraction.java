package com.alechilles.radialmenu.interactions;

import javax.annotation.Nonnull;

import com.alechilles.radialmenu.RadialMenuMod;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecutionMode;
import com.alechilles.radialmenu.runtime.RadialMenuRuntimeService;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class RadialMenuInteraction extends SimpleInteraction {
    public static final String COMMAND_OPEN_MENU = "OpenMenu";
    public static final String COMMAND_EXECUTE_SELECTED = "ExecuteSelected";

    public static final BuilderCodec<RadialMenuInteraction> CODEC = BuilderCodec.builder(
                    RadialMenuInteraction.class,
                    RadialMenuInteraction::new,
                    SimpleInteraction.CODEC
            )
            .documentation("Opens a radial menu or executes the selected option from a RadialMenuConfig.")
            .<String>appendInherited(
                    new KeyedCodec<>("MenuId", Codec.STRING),
                    (interaction, value) -> interaction.menuId = value,
                    interaction -> interaction.menuId,
                    (interaction, parent) -> interaction.menuId = parent.menuId
            )
            .add()
            .<String>appendInherited(
                    new KeyedCodec<>("CommandId", Codec.STRING),
                    (interaction, value) -> interaction.commandId = value,
                    interaction -> interaction.commandId,
                    (interaction, parent) -> interaction.commandId = parent.commandId
            )
            .add()
            .<String>appendInherited(
                    new KeyedCodec<>("ExecutionMode", Codec.STRING),
                    (interaction, value) -> interaction.executionMode = value,
                    interaction -> interaction.executionMode,
                    (interaction, parent) -> interaction.executionMode = parent.executionMode
            )
            .add()
            .build();

    private String menuId;
    private String commandId;
    private String executionMode;

    private RadialMenuInteraction() {
        super();
    }

    public RadialMenuInteraction(String id) {
        super(id);
    }

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Server;
    }

    @Override
    protected void tick0(boolean firstRun,
                         float time,
                         @Nonnull InteractionType type,
                         @Nonnull InteractionContext context,
                         @Nonnull CooldownHandler cooldownHandler) {
        if (!firstRun) {
            super.tick0(false, time, type, context, cooldownHandler);
            return;
        }

        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        Ref<EntityStore> playerRef = context.getEntity();
        if (commandBuffer == null || playerRef == null) {
            context.getState().state = InteractionState.Failed;
            super.tick0(true, time, type, context, cooldownHandler);
            return;
        }

        Player player = commandBuffer.getComponent(playerRef, Player.getComponentType());
        ItemStack heldItem = context.getHeldItem();
        RadialMenuMod plugin = RadialMenuMod.getInstance();
        RadialMenuRuntimeService runtimeService = plugin != null ? plugin.getRuntimeService() : null;
        if (player == null || runtimeService == null) {
            context.getState().state = InteractionState.Failed;
            super.tick0(true, time, type, context, cooldownHandler);
            return;
        }

        String heldItemId = heldItem == null || heldItem.isEmpty() ? null : heldItem.getItemId();
        String resolvedMenuKey = runtimeService.resolveMenuKey(menuId, heldItemId);
        ExecutionMode modeOverride = parseModeOverride(executionMode);

        final boolean[] executed = new boolean[] {false};
        commandBuffer.run(store -> {
            if (isExecuteSelectedCommand(commandId)) {
                executed[0] = runtimeService.executeSelected(player, resolvedMenuKey, modeOverride, "interaction");
                return;
            }
            executed[0] = runtimeService.openMenu(player, resolvedMenuKey, modeOverride, "interaction");
        });

        if (!executed[0]) {
            context.getState().state = InteractionState.Failed;
        }
        context.setHeldItem(heldItem);
        super.tick0(true, time, type, context, cooldownHandler);
    }

    @Override
    protected void simulateTick0(boolean firstRun,
                                 float time,
                                 @Nonnull InteractionType type,
                                 @Nonnull InteractionContext context,
                                 @Nonnull CooldownHandler cooldownHandler) {
        if (context.getServerState() != null && context.getServerState().state == InteractionState.Failed) {
            context.getState().state = InteractionState.Failed;
        }
        super.tick0(firstRun, time, type, context, cooldownHandler);
    }

    private boolean isExecuteSelectedCommand(String value) {
        return value != null && COMMAND_EXECUTE_SELECTED.equalsIgnoreCase(value.trim());
    }

    private static ExecutionMode parseModeOverride(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (ExecutionMode mode : ExecutionMode.values()) {
            if (mode.name().equalsIgnoreCase(value.trim())) {
                return mode;
            }
        }
        return null;
    }
}
