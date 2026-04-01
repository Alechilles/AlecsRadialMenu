package com.alechilles.radialmenu.runtime;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alechilles.radialmenu.api.RadialMenuActionHandler;

public final class RadialMenuActionRegistry {
    private final ConcurrentHashMap<String, RadialMenuActionHandler> handlers = new ConcurrentHashMap<>();

    @Nonnull
    public AutoCloseable register(@Nonnull String actionId, @Nonnull RadialMenuActionHandler handler) {
        String normalizedId = requireNormalized(actionId);
        RadialMenuActionHandler normalizedHandler = Objects.requireNonNull(handler, "handler");
        handlers.put(normalizedId, normalizedHandler);
        return () -> handlers.remove(normalizedId, normalizedHandler);
    }

    @Nullable
    public RadialMenuActionHandler get(@Nullable String actionId) {
        String normalized = RadialMenuCatalog.normalizeKey(actionId);
        if (normalized == null) {
            return null;
        }
        return handlers.get(normalized);
    }

    @Nonnull
    public Set<String> listActionIds() {
        return Set.copyOf(handlers.keySet());
    }

    @Nonnull
    private static String requireNormalized(@Nullable String actionId) {
        String normalized = RadialMenuCatalog.normalizeKey(actionId);
        if (normalized == null) {
            throw new IllegalArgumentException("actionId must be nonblank.");
        }
        return normalized;
    }
}
