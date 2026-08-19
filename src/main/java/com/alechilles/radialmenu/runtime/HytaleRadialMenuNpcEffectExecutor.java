package com.alechilles.radialmenu.runtime;

import java.util.UUID;

import com.alechilles.radialmenu.api.RadialMenuNpcTarget;
import com.alechilles.radialmenu.npc.NpcSupportAccess;
import com.alechilles.radialmenu.npc.RadialMenuResultComponent;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.support.StateSupport;

final class HytaleRadialMenuNpcEffectExecutor implements RadialMenuNpcEffectExecutor {
    static final long RESULT_TTL_MILLIS = 5_000L;

    @Override
    public boolean setState(RadialMenuNpcTarget target, String state, String subState) {
        if (target == null || state == null || state.isBlank() || target.npc().getRole() == null) {
            return false;
        }
        try {
            StateSupport stateSupport = NpcSupportAccess.state(
                    target.npc().getRole(),
                    target.reference(),
                    target.store()
            );
            if (stateSupport == null) {
                return false;
            }
            int stateIndex = stateSupport.getStateHelper().getStateIndex(state.trim());
            if (stateIndex < 0) {
                return false;
            }
            String resolvedSubState = subState == null || subState.isBlank()
                    ? stateSupport.getStateHelper().getDefaultSubState()
                    : subState.trim();
            int subStateIndex = stateSupport.getStateHelper().getSubStateIndex(stateIndex, resolvedSubState);
            if (subStateIndex < 0) {
                return false;
            }
            stateSupport.setState(stateIndex, subStateIndex, true, false);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    @Override
    public boolean emitResult(RadialMenuNpcTarget target,
                              UUID playerUuid,
                              String menuKey,
                              String resultId,
                              long nowMillis) {
        ComponentType<EntityStore, RadialMenuResultComponent> componentType =
                RadialMenuResultComponent.getComponentType();
        if (target == null || playerUuid == null || componentType == null) {
            return false;
        }
        try {
            RadialMenuResultComponent component = target.store().getComponent(target.reference(), componentType);
            if (component == null) {
                component = new RadialMenuResultComponent();
                target.store().putComponent(target.reference(), componentType, component);
            }
            component.put(playerUuid, menuKey, resultId, nowMillis, RESULT_TTL_MILLIS);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
