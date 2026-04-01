package com.alechilles.radialmenu.localization;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class RadialMenuLocalizedText {
    private static final String DEFAULT_LANGUAGE = "en-US";
    private static final String FALLBACK_RESOURCE = "/Server/Languages/en-US/server.lang";
    private static volatile Map<String, String> fallbackEntries;

    private RadialMenuLocalizedText() {
    }

    @Nonnull
    public static String resolve(@Nullable Player player, @Nonnull String key) {
        String language = DEFAULT_LANGUAGE;
        if (player != null && player.getPlayerRef() != null) {
            language = player.getPlayerRef().getLanguage();
        }
        return resolve(language, key);
    }

    @Nonnull
    public static String resolve(@Nullable PlayerRef playerRef, @Nonnull String key) {
        String language = playerRef != null ? playerRef.getLanguage() : DEFAULT_LANGUAGE;
        return resolve(language, key);
    }

    @Nonnull
    public static String resolve(@Nullable String language, @Nonnull String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        String normalizedLanguage = language == null || language.isBlank() ? DEFAULT_LANGUAGE : language;
        String normalizedKey = key.trim();

        I18nModule i18n;
        try {
            i18n = I18nModule.get();
        } catch (Throwable ignored) {
            i18n = null;
        }
        if (i18n != null) {
            try {
                String translated = i18n.getMessage(normalizedLanguage, normalizedKey);
                if (translated != null && !translated.isBlank() && !translated.equals(normalizedKey)) {
                    return translated;
                }
            } catch (Throwable ignored) {
                // fall back below
            }
        }
        String fallback = fallbackMap().get(normalizedKey);
        return fallback != null && !fallback.isBlank() ? fallback : normalizedKey;
    }

    @Nonnull
    public static String format(@Nullable Player player, @Nonnull String key, Object... args) {
        return formatTemplate(resolve(player, key), args);
    }

    @Nonnull
    public static String format(@Nullable PlayerRef playerRef, @Nonnull String key, Object... args) {
        return formatTemplate(resolve(playerRef, key), args);
    }

    @Nonnull
    public static String formatTemplate(@Nullable String template, Object... args) {
        String out = template == null ? "" : template;
        if (args == null || args.length == 0) {
            return out;
        }
        for (int i = 0; i < args.length; i++) {
            String replacement = args[i] == null ? "" : String.valueOf(args[i]);
            out = out.replace("{" + i + "}", replacement);
        }
        return out;
    }

    @Nonnull
    private static Map<String, String> fallbackMap() {
        Map<String, String> map = fallbackEntries;
        if (map != null) {
            return map;
        }
        synchronized (RadialMenuLocalizedText.class) {
            map = fallbackEntries;
            if (map == null) {
                map = loadFallbackEntries();
                fallbackEntries = map;
            }
        }
        return map;
    }

    @Nonnull
    private static Map<String, String> loadFallbackEntries() {
        Map<String, String> out = new HashMap<>();
        try (InputStream stream = RadialMenuLocalizedText.class.getResourceAsStream(FALLBACK_RESOURCE)) {
            if (stream == null) {
                return out;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) {
                        continue;
                    }
                    int split = trimmed.indexOf('=');
                    if (split <= 0 || split == trimmed.length() - 1) {
                        continue;
                    }
                    String key = trimmed.substring(0, split).trim();
                    String value = trimmed.substring(split + 1).trim();
                    if (!key.isBlank() && !value.isBlank()) {
                        out.put(key, value);
                    }
                }
            }
        } catch (Exception ignored) {
            return out;
        }
        return out;
    }
}
