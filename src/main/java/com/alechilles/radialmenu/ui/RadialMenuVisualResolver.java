package com.alechilles.radialmenu.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.config.RadialMenuConfig.Option;
import com.alechilles.radialmenu.config.RadialMenuConfig.OptionVisualOverride;
import com.alechilles.radialmenu.config.RadialMenuConfig.RenderMode;
import com.alechilles.radialmenu.config.RadialMenuConfig.StateColors;
import com.alechilles.radialmenu.config.RadialMenuConfig.StatePalette;
import com.alechilles.radialmenu.config.RadialMenuConfig.TexturePreset;
import com.alechilles.radialmenu.config.RadialMenuConfig.TextureSet;

public final class RadialMenuVisualResolver {
    public static final String LEGACY_TEXTURE_PREFIX = "RadialMenu";

    private static final String[] SLICE_STATES = new String[] {"Default", "Hover", "Pressed"};

    private RadialMenuVisualResolver() {
    }

    @Nonnull
    public static ResolvedOptionVisual resolveOptionVisual(@Nonnull RadialMenuConfig config,
                                                           @Nonnull Option option,
                                                           boolean selected) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(option, "option");

        RadialMenuConfig.Visual visual = config.getVisual();
        StatePalette basePalette = visual.getStates();
        OptionVisualOverride override = option.getVisualOverride();
        StatePalette overridePalette = override == null ? null : override.getStates();

        ResolvedState defaultState = resolveState(
                basePalette.getDefaultState(),
                overridePalette == null ? null : overridePalette.getDefaultStateRaw()
        );
        ResolvedState hoverState = resolveState(
                basePalette.getHoverState(),
                overridePalette == null ? null : overridePalette.getHoverStateRaw()
        );
        ResolvedState pressedState = resolveState(
                basePalette.getPressedState(),
                overridePalette == null ? null : overridePalette.getPressedStateRaw()
        );
        ResolvedState selectedState = resolveState(
                basePalette.getSelectedState(),
                overridePalette == null ? null : overridePalette.getSelectedStateRaw()
        );
        ResolvedState disabledState = resolveState(
                basePalette.getDisabledState(),
                overridePalette == null ? null : overridePalette.getDisabledStateRaw()
        );

        int labelFontSize = visual.getLabel().getFontSize();
        if (override != null && override.getLabelFontSize() != null && override.getLabelFontSize() > 0) {
            labelFontSize = override.getLabelFontSize();
        }

        return new ResolvedOptionVisual(
                labelFontSize,
                selected ? selectedState : defaultState,
                hoverState,
                pressedState,
                selectedState,
                disabledState
        );
    }

    @Nonnull
    public static String resolveTexturePrefix(@Nonnull RadialMenuConfig config,
                                              @Nullable Predicate<String> prefixCompletenessCheck,
                                              @Nullable Consumer<String> warningSink) {
        Objects.requireNonNull(config, "config");

        RenderMode mode = config.getVisual().getRenderMode();
        if (mode != RenderMode.Texture) {
            return LEGACY_TEXTURE_PREFIX;
        }

        TextureSet textureSet = config.getVisual().getTextureSet();
        String prefix = normalizePrefix(textureSet.getPrefix());
        if (prefix == null || prefix.isBlank()) {
            TexturePreset preset = textureSet.getPreset();
            if (preset == TexturePreset.LegacyDefault) {
                return LEGACY_TEXTURE_PREFIX;
            }
            return LEGACY_TEXTURE_PREFIX;
        }

        if (prefixCompletenessCheck != null && !prefixCompletenessCheck.test(prefix)) {
            if (warningSink != null) {
                warningSink.accept(
                        "Texture set prefix '" + prefix + "' is incomplete. Falling back to '" + LEGACY_TEXTURE_PREFIX + "'."
                );
            }
            return LEGACY_TEXTURE_PREFIX;
        }

        return prefix;
    }

    @Nonnull
    public static List<String> expectedTextureFiles(@Nonnull String prefix) {
        String normalizedPrefix = normalizePrefix(prefix);
        if (normalizedPrefix == null || normalizedPrefix.isBlank()) {
            return List.of();
        }
        List<String> expected = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (String state : SLICE_STATES) {
                expected.add(normalizedPrefix + "/CommandWheelSlice" + i + "_" + state + ".png");
            }
        }
        expected.add(normalizedPrefix + "/CommandWheelRingOuter.png");
        expected.add(normalizedPrefix + "/CommandWheelRingInner.png");
        expected.add(normalizedPrefix + "/CommandWheelCenterPanel.png");
        return List.copyOf(expected);
    }

    public static boolean textureSetLooksComplete(@Nonnull String prefix, @Nonnull Function<String, Boolean> resourceExists) {
        Objects.requireNonNull(resourceExists, "resourceExists");
        for (String path : expectedTextureFiles(prefix)) {
            if (!Boolean.TRUE.equals(resourceExists.apply(path))) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public static String normalizePrefix(@Nullable String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return null;
        }
        String normalized = prefix.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isBlank()) {
            return null;
        }
        return normalized;
    }

    @Nonnull
    private static ResolvedState resolveState(@Nonnull StateColors base, @Nullable StateColors override) {
        StateColors merged = StateColors.merge(base, override);
        return new ResolvedState(
                normalizeColor(merged.getFillColor()),
                normalizeColor(merged.getTextColor()),
                normalizeColor(merged.getBorderColor())
        );
    }

    @Nonnull
    private static String normalizeColor(@Nullable String color) {
        if (color == null || color.isBlank()) {
            return "#ffffff";
        }
        return color.trim().toLowerCase(Locale.ROOT);
    }

    public record ResolvedState(@Nonnull String fillColor,
                                @Nonnull String textColor,
                                @Nonnull String borderColor) {
    }

    public record ResolvedOptionVisual(int labelFontSize,
                                       @Nonnull ResolvedState defaultState,
                                       @Nonnull ResolvedState hoverState,
                                       @Nonnull ResolvedState pressedState,
                                       @Nonnull ResolvedState selectedState,
                                       @Nonnull ResolvedState disabledState) {
    }
}

