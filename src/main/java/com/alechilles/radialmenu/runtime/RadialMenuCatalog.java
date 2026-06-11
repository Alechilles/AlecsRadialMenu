package com.alechilles.radialmenu.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecuteCommandOption;
import com.alechilles.radialmenu.config.RadialMenuConfig.InvokeRegisteredActionOption;
import com.alechilles.radialmenu.config.RadialMenuConfig.Option;
import com.alechilles.radialmenu.config.RadialMenuConfig.OptionVisualOverride;
import com.alechilles.radialmenu.config.RadialMenuConfig.StateColors;
import com.alechilles.radialmenu.config.RadialMenuConfig.StatePalette;
import com.alechilles.radialmenu.config.RadialMenuConfig.Visual;
import com.hypixel.hytale.logger.HytaleLogger;

public final class RadialMenuCatalog {
    public static final int MAX_OPTIONS = 8;
    private static final Pattern COLOR_PATTERN = Pattern.compile(
            "^#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})(\\((?:0(?:\\.\\d+)?|1(?:\\.0+)?)\\))?$"
    );

    private final Map<String, RadialMenuConfig> menusByNormalizedKey = new LinkedHashMap<>();
    private final Map<String, String> itemToMenuKey = new LinkedHashMap<>();
    private final Set<String> menuKeys = new LinkedHashSet<>();

    public synchronized void rebuild(@Nullable Map<String, RadialMenuConfig> source, @Nullable HytaleLogger logger) {
        menusByNormalizedKey.clear();
        itemToMenuKey.clear();
        menuKeys.clear();
        if (source == null || source.isEmpty()) {
            return;
        }
        for (Map.Entry<String, RadialMenuConfig> entry : source.entrySet()) {
            String menuKey = normalizeKey(entry.getKey());
            RadialMenuConfig menu = entry.getValue();
            if (menuKey == null || menu == null || !menu.isEnabled()) {
                continue;
            }
            List<String> issues = validate(menu);
            if (!issues.isEmpty()) {
                if (logger != null) {
                    logger.at(java.util.logging.Level.WARNING).log(
                            "RadialMenu skipped menu '" + menuKey + "': " + String.join("; ", issues)
                    );
                }
                continue;
            }
            menusByNormalizedKey.put(menuKey, menu);
            menuKeys.add(menuKey);
            for (String itemId : menu.getItemIds()) {
                String normalizedItem = normalizeKey(itemId);
                if (normalizedItem == null || itemToMenuKey.containsKey(normalizedItem)) {
                    continue;
                }
                itemToMenuKey.put(normalizedItem, menuKey);
            }
        }
    }

    @Nonnull
    public synchronized Set<String> listMenuKeys() {
        return Set.copyOf(menuKeys);
    }

    @Nullable
    public synchronized RadialMenuConfig getByMenuKey(@Nullable String menuKey) {
        String normalized = normalizeKey(menuKey);
        if (normalized == null) {
            return null;
        }
        return menusByNormalizedKey.get(normalized);
    }

    @Nullable
    public synchronized String resolveMenuKeyForItem(@Nullable String itemId) {
        String normalized = normalizeKey(itemId);
        if (normalized == null) {
            return null;
        }
        return itemToMenuKey.get(normalized);
    }

    @Nonnull
    public List<String> validate(@Nonnull RadialMenuConfig menu) {
        RadialMenuConfig normalizedMenu = menu;
        List<String> issues = new ArrayList<>();
        Option[] options = normalizedMenu.getOptions();
        if (options == null || options.length == 0) {
            issues.add("Options must contain at least one entry.");
            return issues;
        }
        if (options.length > MAX_OPTIONS) {
            issues.add("Options exceeds hard cap of " + MAX_OPTIONS + ".");
        }
        Set<String> optionIds = new LinkedHashSet<>();
        for (Option option : options) {
            if (option == null) {
                issues.add("Options cannot contain null entries.");
                continue;
            }
            String optionId = normalizeKey(option.getId());
            if (optionId == null) {
                issues.add("Option Id must be nonblank.");
                continue;
            }
            if (!optionIds.add(optionId)) {
                issues.add("Duplicate option Id: " + option.getId());
            }
            validateOptionVisualOverride(option, issues);
            if (option instanceof ExecuteCommandOption executeCommandOption) {
                if (executeCommandOption.getCommand() == null || executeCommandOption.getCommand().isBlank()) {
                    issues.add("ExecuteCommand option '" + option.getId() + "' has blank Command.");
                }
                continue;
            }
            if (option instanceof InvokeRegisteredActionOption invokeRegisteredActionOption) {
                if (invokeRegisteredActionOption.getActionId() == null
                        || invokeRegisteredActionOption.getActionId().isBlank()) {
                    issues.add("InvokeRegisteredAction option '" + option.getId() + "' has blank ActionId.");
                }
                continue;
            }
            issues.add("Unsupported option type for '" + option.getId() + "'.");
        }
        String defaultOptionId = normalizeKey(normalizedMenu.getDefaultOptionId());
        if (defaultOptionId != null && !optionIds.contains(defaultOptionId)) {
            issues.add("DefaultOptionId does not match any option Id.");
        }

        validateVisual(normalizedMenu.getVisual(), issues);
        return Collections.unmodifiableList(issues);
    }

    private void validateVisual(@Nonnull Visual visual, @Nonnull List<String> issues) {
        int outer = visual.getGeometry().getOuterDiameterPx();
        int inner = visual.getGeometry().getInnerDiameterPx();
        int labelRadius = visual.getGeometry().getLabelRadiusPx();
        int center = visual.getGeometry().getCenterDiameterPx();

        if (outer <= 0) {
            issues.add("Visual.Geometry.OuterDiameterPx must be > 0.");
        }
        if (inner <= 0) {
            issues.add("Visual.Geometry.InnerDiameterPx must be > 0.");
        }
        if (outer > 0 && inner > 0 && inner >= outer) {
            issues.add("Visual.Geometry.InnerDiameterPx must be smaller than OuterDiameterPx.");
        }
        if (center <= 0) {
            issues.add("Visual.Geometry.CenterDiameterPx must be > 0.");
        }
        if (inner > 0 && center > inner) {
            issues.add("Visual.Geometry.CenterDiameterPx must be <= InnerDiameterPx.");
        }
        if (labelRadius <= 0) {
            issues.add("Visual.Geometry.LabelRadiusPx must be > 0.");
        } else {
            double minRadius = inner / 2.0;
            double maxRadius = outer / 2.0;
            if (outer > 0 && inner > 0 && (labelRadius <= minRadius || labelRadius >= maxRadius)) {
                issues.add("Visual.Geometry.LabelRadiusPx must be between inner and outer radii.");
            }
        }

        if (visual.getBorderThicknessPx() < 0) {
            issues.add("Visual.BorderThicknessPx must be >= 0.");
        }
        if (visual.getLabel().getFontSize() <= 0) {
            issues.add("Visual.Label.FontSize must be > 0.");
        }

        validateStatePalette("Visual.States", visual.getStates(), issues, false);

    }

    private void validateOptionVisualOverride(@Nonnull Option option, @Nonnull List<String> issues) {
        OptionVisualOverride override = option.getVisualOverride();
        if (override == null) {
            return;
        }
        if (override.getLabelFontSize() != null && override.getLabelFontSize() <= 0) {
            issues.add("Option '" + option.getId() + "' VisualOverride.LabelFontSize must be > 0.");
        }
        StatePalette palette = override.getStates();
        if (palette != null) {
            validateStatePalette(
                    "Option '" + option.getId() + "' VisualOverride.States",
                    palette,
                    issues,
                    true
            );
        }
    }

    private void validateStatePalette(@Nonnull String owner,
                                      @Nonnull StatePalette palette,
                                      @Nonnull List<String> issues,
                                      boolean allowMissingStates) {
        validateStateColors(owner + ".Default", allowMissingStates ? palette.getDefaultStateRaw() : palette.getDefaultState(), issues, allowMissingStates);
        validateStateColors(owner + ".Hover", allowMissingStates ? palette.getHoverStateRaw() : palette.getHoverState(), issues, allowMissingStates);
        validateStateColors(owner + ".Pressed", allowMissingStates ? palette.getPressedStateRaw() : palette.getPressedState(), issues, allowMissingStates);
        validateStateColors(owner + ".Selected", allowMissingStates ? palette.getSelectedStateRaw() : palette.getSelectedState(), issues, allowMissingStates);
        validateStateColors(owner + ".Disabled", allowMissingStates ? palette.getDisabledStateRaw() : palette.getDisabledState(), issues, allowMissingStates);
    }

    private void validateStateColors(@Nonnull String owner,
                                     @Nullable StateColors colors,
                                     @Nonnull List<String> issues,
                                     boolean allowMissingState) {
        if (colors == null) {
            if (!allowMissingState) {
                issues.add(owner + " must be defined.");
            }
            return;
        }
        validateColor(owner + ".FillColor", colors.getFillColor(), issues, allowMissingState);
        validateColor(owner + ".TextColor", colors.getTextColor(), issues, allowMissingState);
        validateColor(owner + ".BorderColor", colors.getBorderColor(), issues, allowMissingState);
    }

    private void validateColor(@Nonnull String field,
                               @Nullable String value,
                               @Nonnull List<String> issues,
                               boolean allowMissing) {
        if (value == null || value.isBlank()) {
            if (!allowMissing) {
                issues.add(field + " cannot be blank.");
            }
            return;
        }
        if (!COLOR_PATTERN.matcher(value.trim()).matches()) {
            issues.add(field + " is not a valid hex color string.");
        }
    }

    @Nullable
    public static String normalizeKey(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
