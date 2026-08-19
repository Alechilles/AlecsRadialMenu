package com.alechilles.radialmenu.npc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Holds short-lived radial menu results that an NPC instruction sensor can consume. */
public final class RadialMenuResultComponent implements Component<EntityStore> {
    private static final PendingResult[] EMPTY_RESULTS = new PendingResult[0];

    private static final BuilderCodec<PendingResult> RESULT_CODEC = BuilderCodec.builder(
                    PendingResult.class,
                    PendingResult::new
            )
            .<UUID>append(
                    new KeyedCodec<>("PlayerUuid", Codec.UUID_BINARY),
                    (result, value) -> result.playerUuid = value,
                    result -> result.playerUuid
            )
            .add()
            .<String>append(
                    new KeyedCodec<>("MenuKey", Codec.STRING),
                    (result, value) -> result.menuKey = value,
                    result -> result.menuKey
            )
            .add()
            .<String>append(
                    new KeyedCodec<>("ResultId", Codec.STRING),
                    (result, value) -> result.resultId = value,
                    result -> result.resultId
            )
            .add()
            .<Long>append(
                    new KeyedCodec<>("CreatedAtMillis", Codec.LONG),
                    (result, value) -> result.createdAtMillis = value == null ? 0L : value,
                    result -> result.createdAtMillis
            )
            .add()
            .<Long>append(
                    new KeyedCodec<>("ExpiresAtMillis", Codec.LONG),
                    (result, value) -> result.expiresAtMillis = value == null ? 0L : value,
                    result -> result.expiresAtMillis
            )
            .add()
            .build();

    public static final BuilderCodec<RadialMenuResultComponent> CODEC = BuilderCodec.builder(
                    RadialMenuResultComponent.class,
                    RadialMenuResultComponent::new
            )
            .<PendingResult[]>append(
                    new KeyedCodec<>("Results", new ArrayCodec<>(RESULT_CODEC, PendingResult[]::new)),
                    (component, value) -> component.results = value == null ? EMPTY_RESULTS : value,
                    component -> component.results
            )
            .add()
            .build();

    private PendingResult[] results = EMPTY_RESULTS;
    private static volatile ComponentType<EntityStore, RadialMenuResultComponent> componentType;

    @Nonnull
    public static synchronized ComponentType<EntityStore, RadialMenuResultComponent> register(
            @Nonnull JavaPlugin plugin) {
        if (componentType == null) {
            componentType = plugin.getEntityStoreRegistry().registerComponent(
                    RadialMenuResultComponent.class,
                    "RadialMenuResult",
                    CODEC
            );
        }
        return componentType;
    }

    public static ComponentType<EntityStore, RadialMenuResultComponent> getComponentType() {
        return componentType;
    }

    public void put(@Nonnull UUID playerUuid,
                    @Nonnull String menuKey,
                    @Nonnull String resultId,
                    long nowMillis,
                    long ttlMillis) {
        if (ttlMillis <= 0L) {
            throw new IllegalArgumentException("ttlMillis must be greater than zero");
        }
        String normalizedMenuKey = requireIdentifier(menuKey, "menuKey");
        String normalizedResultId = requireIdentifier(resultId, "resultId");
        long expiresAtMillis = nowMillis > Long.MAX_VALUE - ttlMillis
                ? Long.MAX_VALUE
                : nowMillis + ttlMillis;

        List<PendingResult> retained = retainedResults(nowMillis, playerUuid);
        retained.add(new PendingResult(
                playerUuid,
                normalizedMenuKey,
                normalizedResultId,
                nowMillis,
                expiresAtMillis
        ));
        results = retained.toArray(PendingResult[]::new);
    }

    public boolean consume(@Nonnull UUID playerUuid,
                           @Nonnull String menuKey,
                           @Nonnull String resultId,
                           long nowMillis) {
        String normalizedMenuKey = normalizeIdentifier(menuKey);
        String normalizedResultId = normalizeIdentifier(resultId);
        if (normalizedMenuKey == null || normalizedResultId == null) {
            prune(nowMillis);
            return false;
        }

        boolean matched = false;
        List<PendingResult> retained = new ArrayList<>(results.length);
        for (PendingResult result : results) {
            if (!isLive(result, nowMillis)) {
                continue;
            }
            if (!matched
                    && playerUuid.equals(result.playerUuid)
                    && normalizedMenuKey.equals(result.menuKey)
                    && normalizedResultId.equals(result.resultId)) {
                matched = true;
                continue;
            }
            retained.add(result);
        }
        results = retained.toArray(PendingResult[]::new);
        return matched;
    }

    public void prune(long nowMillis) {
        results = Arrays.stream(results)
                .filter(result -> isLive(result, nowMillis))
                .toArray(PendingResult[]::new);
    }

    @Override
    public RadialMenuResultComponent clone() {
        RadialMenuResultComponent copy = new RadialMenuResultComponent();
        copy.results = Arrays.stream(results)
                .map(PendingResult::copy)
                .toArray(PendingResult[]::new);
        return copy;
    }

    private List<PendingResult> retainedResults(long nowMillis, UUID replacedPlayerUuid) {
        List<PendingResult> retained = new ArrayList<>(results.length + 1);
        for (PendingResult result : results) {
            if (isLive(result, nowMillis) && !replacedPlayerUuid.equals(result.playerUuid)) {
                retained.add(result);
            }
        }
        return retained;
    }

    private static boolean isLive(PendingResult result, long nowMillis) {
        return result != null
                && result.playerUuid != null
                && result.menuKey != null
                && result.resultId != null
                && result.expiresAtMillis > nowMillis;
    }

    private static String requireIdentifier(String value, String fieldName) {
        String normalized = normalizeIdentifier(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private static String normalizeIdentifier(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static final class PendingResult {
        private UUID playerUuid;
        private String menuKey;
        private String resultId;
        private long createdAtMillis;
        private long expiresAtMillis;

        private PendingResult() {
        }

        private PendingResult(UUID playerUuid,
                              String menuKey,
                              String resultId,
                              long createdAtMillis,
                              long expiresAtMillis) {
            this.playerUuid = playerUuid;
            this.menuKey = menuKey;
            this.resultId = resultId;
            this.createdAtMillis = createdAtMillis;
            this.expiresAtMillis = expiresAtMillis;
        }

        private PendingResult copy() {
            return new PendingResult(playerUuid, menuKey, resultId, createdAtMillis, expiresAtMillis);
        }
    }
}
