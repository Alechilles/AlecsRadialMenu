package com.alechilles.radialmenu.npc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
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

    public boolean matches(@Nonnull Ref<EntityStore> npcRef,
                           @Nonnull Role role,
                           double dt,
                           @Nonnull Store<EntityStore> store) {
        if (once && triggered) {
            return false;
        }
        return matchesResult(npcRef, NpcSupportAccess.state(role, npcRef, store), store);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> npcRef,
                           @Nonnull ExecutionSupport support,
                           double dt,
                           @Nonnull Store<EntityStore> store) {
        if (!super.matches(npcRef, support, dt, store)) {
            return false;
        }
        return matchesResult(npcRef, support.getStateSupport(), store);
    }

    private boolean matchesResult(@Nonnull Ref<EntityStore> npcRef,
                                  @Nullable StateSupport stateSupport,
                                  @Nonnull Store<EntityStore> store) {
        if (stateSupport == null) {
            return false;
        }
        Ref<EntityStore> playerRef = stateSupport.getInteractionIterationTarget();
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
