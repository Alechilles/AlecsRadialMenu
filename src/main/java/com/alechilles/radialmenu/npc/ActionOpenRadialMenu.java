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
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;

public final class ActionOpenRadialMenu extends ActionBase {
    private final String menuId;

    public ActionOpenRadialMenu(@Nonnull BuilderActionOpenRadialMenu builder,
                                @Nonnull BuilderSupport support) {
        super(builder);
        this.menuId = builder.getMenuId(support);
    }

    @Override
    public boolean canExecute(@Nonnull Ref<EntityStore> npcRef,
                              @Nonnull Role role,
                              @Nullable InfoProvider sensorInfo,
                              double dt,
                              @Nonnull Store<EntityStore> store) {
        return super.canExecute(npcRef, role, sensorInfo, dt, store)
                && resolveInteractionPlayer(role, store) != null
                && RadialMenuMod.getApiInstance() != null;
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef,
                           @Nonnull Role role,
                           @Nullable InfoProvider sensorInfo,
                           double dt,
                           @Nonnull Store<EntityStore> store) {
        Player player = resolveInteractionPlayer(role, store);
        RadialMenuApi api = RadialMenuMod.getApiInstance();
        if (player == null || api == null || !api.openNpcMenu(player, menuId, npcRef, store)) {
            return false;
        }
        return super.execute(npcRef, role, sensorInfo, dt, store);
    }

    @Nullable
    private static Player resolveInteractionPlayer(@Nonnull Role role,
                                                   @Nonnull Store<EntityStore> store) {
        Ref<EntityStore> playerRef = role.getStateSupport().getInteractionIterationTarget();
        if (playerRef == null || !playerRef.isValid()) {
            return null;
        }
        return store.getComponent(playerRef, Player.getComponentType());
    }
}
