package com.alechilles.radialmenu.npc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;

public final class SensorRadialMenuResult extends SensorBase {
    private final String menuId;
    private final String resultId;

    public SensorRadialMenuResult(@Nonnull BuilderSensorRadialMenuResult builder,
                                  @Nonnull BuilderSupport support) {
        super(builder);
        this.menuId = builder.getMenuId(support);
        this.resultId = builder.getResultId(support);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> npcRef,
                           @Nonnull Role role,
                           double dt,
                           @Nonnull Store<EntityStore> store) {
        if (!super.matches(npcRef, role, dt, store)) {
            return false;
        }
        Ref<EntityStore> playerRef = role.getStateSupport().getInteractionIterationTarget();
        if (playerRef == null || !playerRef.isValid()) {
            return false;
        }
        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) {
            return false;
        }
        boolean matched = RadialMenuResultMatcher.consume(
                store,
                npcRef,
                RadialMenuResultComponent.getComponentType(),
                player.getUuid(),
                menuId,
                resultId,
                System.currentTimeMillis()
        );
        if (matched) {
            setOnce();
        }
        return matched;
    }

    @Nullable
    @Override
    public InfoProvider getSensorInfo() {
        return null;
    }
}
