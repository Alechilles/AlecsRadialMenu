package com.alechilles.radialmenu;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecuteCommandOption;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecutionMode;
import com.alechilles.radialmenu.config.RadialMenuConfig.InvokeRegisteredActionOption;
import com.alechilles.radialmenu.config.RadialMenuConfig.Option;

public final class TestConfigFactory {
    private TestConfigFactory() {
    }

    public static RadialMenuConfig menu(String key,
                                        ExecutionMode mode,
                                        String defaultOptionId,
                                        String[] itemIds,
                                        Option... options) {
        RadialMenuConfig config = instantiate(RadialMenuConfig.class);
        setField(RadialMenuConfig.class, config, "key", key);
        setField(RadialMenuConfig.class, config, "enabled", true);
        setField(RadialMenuConfig.class, config, "itemIds", itemIds == null ? new String[0] : itemIds);
        setField(RadialMenuConfig.class, config, "executionMode", mode == null ? ExecutionMode.SelectAndArm : mode);
        setField(RadialMenuConfig.class, config, "defaultOptionId", defaultOptionId);
        setField(RadialMenuConfig.class, config, "options", options == null ? new Option[0] : options);
        return config;
    }

    public static ExecuteCommandOption commandOption(String id, String label, String command) {
        ExecuteCommandOption option = instantiate(ExecuteCommandOption.class);
        setField(Option.class, option, "id", id);
        setField(Option.class, option, "label", label);
        setField(ExecuteCommandOption.class, option, "command", command);
        return option;
    }

    public static InvokeRegisteredActionOption actionOption(String id,
                                                            String label,
                                                            String actionId,
                                                            Map<String, String> payload) {
        InvokeRegisteredActionOption option = instantiate(InvokeRegisteredActionOption.class);
        setField(Option.class, option, "id", id);
        setField(Option.class, option, "label", label);
        setField(InvokeRegisteredActionOption.class, option, "actionId", actionId);
        setField(InvokeRegisteredActionOption.class, option, "payload", payload == null ? Map.of() : payload);
        return option;
    }

    private static <T> T instantiate(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to instantiate " + type.getName(), ex);
        }
    }

    private static void setField(Class<?> owner, Object target, String fieldName, Object value) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to set field " + owner.getSimpleName() + "." + fieldName, ex);
        }
    }
}
