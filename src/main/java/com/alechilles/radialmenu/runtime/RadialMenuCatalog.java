package com.alechilles.radialmenu.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecuteCommandOption;
import com.alechilles.radialmenu.config.RadialMenuConfig.InvokeRegisteredActionOption;
import com.alechilles.radialmenu.config.RadialMenuConfig.Option;
import com.hypixel.hytale.logger.HytaleLogger;

public final class RadialMenuCatalog {
    public static final int MAX_OPTIONS = 8;

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
        return Collections.unmodifiableList(issues);
    }

    @Nullable
    public static String normalizeKey(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
