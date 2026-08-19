package com.alechilles.radialmenu.npc;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;

public final class BuilderActionOpenRadialMenu extends BuilderActionBase {
    public static final String BUILDER_ID = "OpenRadialMenu";

    private final StringHolder menuId = new StringHolder();

    public BuilderActionOpenRadialMenu() {
        requireInstructionType(EnumSet.of(InstructionType.Interaction));
    }

    @Nonnull
    @Override
    public Action build(@Nonnull BuilderSupport support) {
        return new ActionOpenRadialMenu(this, support);
    }

    @Nonnull
    @Override
    public BuilderActionOpenRadialMenu readConfig(@Nonnull JsonElement data) {
        requireString(
                data,
                "MenuId",
                menuId,
                StringNotEmptyValidator.get(),
                BuilderDescriptorState.Stable,
                "Radial menu asset id to open for the interacting player.",
                null
        );
        return this;
    }

    @Nonnull
    String getMenuId(@Nonnull BuilderSupport support) {
        return menuId.get(support.getExecutionContext());
    }

    @Nonnull
    @Override
    public String getShortDescription() {
        return "Opens a radial menu for the interacting player.";
    }

    @Nonnull
    @Override
    public String getLongDescription() {
        return "Opens MenuId and carries this NPC as the target for immediate menu actions.";
    }

    @Nonnull
    @Override
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }
}
