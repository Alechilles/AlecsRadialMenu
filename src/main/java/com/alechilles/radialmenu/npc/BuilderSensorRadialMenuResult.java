package com.alechilles.radialmenu.npc;

import java.util.EnumSet;

import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;

public final class BuilderSensorRadialMenuResult extends BuilderSensorBase {
    public static final String BUILDER_ID = "RadialMenuResult";

    private final StringHolder menuId = new StringHolder();
    private final StringHolder resultId = new StringHolder();

    @Nonnull
    @Override
    public Sensor build(@Nonnull BuilderSupport support) {
        return new SensorRadialMenuResult(this, support);
    }

    @Nonnull
    @Override
    public BuilderSensorRadialMenuResult readConfig(@Nonnull JsonElement data) {
        requireString(
                data,
                "MenuId",
                menuId,
                StringNotEmptyValidator.get(),
                BuilderDescriptorState.Stable,
                "Radial menu asset id that emitted the result.",
                null
        );
        requireString(
                data,
                "ResultId",
                resultId,
                StringNotEmptyValidator.get(),
                BuilderDescriptorState.Stable,
                "Named radial result to consume.",
                null
        );
        requireInstructionType(EnumSet.of(InstructionType.Interaction));
        return this;
    }

    @Nonnull
    String getMenuId(@Nonnull BuilderSupport support) {
        return menuId.get(support.getExecutionContext());
    }

    @Nonnull
    String getResultId(@Nonnull BuilderSupport support) {
        return resultId.get(support.getExecutionContext());
    }

    @Nonnull
    @Override
    public String getShortDescription() {
        return "Consumes a matching result from an NPC-opened radial menu.";
    }

    @Nonnull
    @Override
    public String getLongDescription() {
        return "Matches the current interaction player, MenuId, and ResultId once, then removes the result.";
    }

    @Nonnull
    @Override
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }
}
