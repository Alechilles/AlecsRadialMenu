package com.alechilles.radialmenu.npc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.StateSupport;

/** Resolves NPC state support across the Update 5 and Update 6 APIs. */
public final class NpcSupportAccess {
    private static final String UPDATE_6_MARKER =
            "com.hypixel.hytale.server.npc.instructions.ExecutionSupport";
    private static final boolean UPDATE_6_OR_LATER = classExists(UPDATE_6_MARKER);
    private static final MethodHandle LEGACY_STATE = legacyStateGetter();

    private NpcSupportAccess() {
    }

    @Nullable
    public static StateSupport state(@Nullable Role role,
                                     @Nullable Ref<EntityStore> npcRef,
                                     @Nullable ComponentAccessor<EntityStore> accessor) {
        if (UPDATE_6_OR_LATER) {
            return npcRef != null && npcRef.isValid() && accessor != null
                    ? StateSupport.get(npcRef, accessor)
                    : null;
        }
        if (role == null || LEGACY_STATE == null) {
            return null;
        }
        try {
            return (StateSupport) LEGACY_STATE.invoke(role);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Could not resolve Update 5 StateSupport", throwable);
        }
    }

    @Nullable
    private static MethodHandle legacyStateGetter() {
        if (UPDATE_6_OR_LATER) {
            return null;
        }
        try {
            return MethodHandles.publicLookup().findVirtual(
                    Role.class,
                    "getStateSupport",
                    MethodType.methodType(StateSupport.class)
            );
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className, false, NpcSupportAccess.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }
}
