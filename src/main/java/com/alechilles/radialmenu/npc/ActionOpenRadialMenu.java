package com.alechilles.radialmenu.npc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alechilles.radialmenu.RadialMenuMod;
import com.alechilles.radialmenu.api.RadialMenuApi;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;

public final class ActionOpenRadialMenu extends ActionBase {
    private final String menuId;

    public ActionOpenRadialMenu(@Nonnull BuilderActionOpenRadialMenu builder,
                                @Nonnull BuilderSupport support) {
        super(builder);
        this.menuId = builder.getMenuId(support);
    }

    public boolean canExecute(@Nonnull Ref<EntityStore> npcRef,
                              @Nonnull Role role,
                              @Nullable InfoProvider sensorInfo,
                              double dt,
                              @Nonnull Store<EntityStore> store) {
        return (!once || !triggered)
                && resolveInteractionPlayer(NpcSupportAccess.state(role, npcRef, store), store) != null
                && RadialMenuMod.getApiInstance() != null;
    }

    @Override
    public boolean canExecute(@Nonnull Ref<EntityStore> npcRef,
                              @Nonnull ExecutionSupport support,
                              @Nullable InfoProvider sensorInfo,
                              double dt,
                              @Nonnull Store<EntityStore> store) {
        return super.canExecute(npcRef, support, sensorInfo, dt, store)
                && resolveInteractionPlayer(support.getStateSupport(), store) != null
                && RadialMenuMod.getApiInstance() != null;
    }

    public boolean execute(@Nonnull Ref<EntityStore> npcRef,
                           @Nonnull Role role,
                           @Nullable InfoProvider sensorInfo,
                           double dt,
                           @Nonnull Store<EntityStore> store) {
        Player player = resolveInteractionPlayer(NpcSupportAccess.state(role, npcRef, store), store);
        RadialMenuApi api = RadialMenuMod.getApiInstance();
        if (player == null || api == null || !api.openNpcMenu(player, menuId, npcRef, store)) {
            return false;
        }
        setOnce();
        return true;
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef,
                           @Nonnull ExecutionSupport support,
                           @Nullable InfoProvider sensorInfo,
                           double dt,
                           @Nonnull Store<EntityStore> store) {
        Player player = resolveInteractionPlayer(support.getStateSupport(), store);
        RadialMenuApi api = RadialMenuMod.getApiInstance();
        if (player == null || api == null || !api.openNpcMenu(player, menuId, npcRef, store)) {
            return false;
        }
        return super.execute(npcRef, support, sensorInfo, dt, store);
    }

    @Nullable
    private static Player resolveInteractionPlayer(@Nullable StateSupport stateSupport,
                                                   @Nonnull Store<EntityStore> store) {
        if (stateSupport == null) {
            return null;
        }
        Ref<EntityStore> playerRef = stateSupport.getInteractionIterationTarget();
        if (playerRef == null || !playerRef.isValid()) {
            return null;
        }
        return store.getComponent(playerRef, Player.getComponentType());
    }
}
