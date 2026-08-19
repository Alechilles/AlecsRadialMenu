package com.alechilles.radialmenu.runtime;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alechilles.radialmenu.api.RadialMenuNpcActionHandler;

public final class RadialMenuNpcActionRegistry {
    private final ConcurrentHashMap<String, RadialMenuNpcActionHandler> handlers = new ConcurrentHashMap<>();

    @Nonnull
    public AutoCloseable register(@Nonnull String actionId, @Nonnull RadialMenuNpcActionHandler handler) {
        String normalizedId = requireNormalized(actionId);
        RadialMenuNpcActionHandler normalizedHandler = Objects.requireNonNull(handler, "handler");
        if (handlers.putIfAbsent(normalizedId, normalizedHandler) != null) {
            throw new IllegalStateException("NPC action handler is already registered: " + actionId);
        }
        return () -> handlers.remove(normalizedId, normalizedHandler);
    }

    @Nullable
    public RadialMenuNpcActionHandler get(@Nullable String actionId) {
        String normalizedId = RadialMenuCatalog.normalizeKey(actionId);
        return normalizedId == null ? null : handlers.get(normalizedId);
    }

    @Nonnull
    public Set<String> listActionIds() {
        return Set.copyOf(handlers.keySet());
    }

    @Nonnull
    private static String requireNormalized(@Nullable String actionId) {
        String normalizedId = RadialMenuCatalog.normalizeKey(actionId);
        if (normalizedId == null) {
            throw new IllegalArgumentException("actionId must be nonblank.");
        }
        return normalizedId;
    }
}
