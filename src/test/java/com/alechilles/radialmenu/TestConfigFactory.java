package com.alechilles.radialmenu;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecuteCommandOption;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecutionMode;
import com.alechilles.radialmenu.config.RadialMenuConfig.InvokeRegisteredActionOption;
import com.alechilles.radialmenu.config.RadialMenuConfig.Option;
import com.alechilles.radialmenu.config.RadialMenuConfig.OptionVisualOverride;
import com.alechilles.radialmenu.config.RadialMenuConfig.RenderMode;
import com.alechilles.radialmenu.config.RadialMenuConfig.StateColors;
import com.alechilles.radialmenu.config.RadialMenuConfig.StatePalette;
import com.alechilles.radialmenu.config.RadialMenuConfig.TextureSet;
import com.alechilles.radialmenu.config.RadialMenuConfig.Visual;

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

    public static void setVisual(RadialMenuConfig config, Visual visual) {
        setField(RadialMenuConfig.class, config, "visual", visual);
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

    public static void setOptionVisualOverride(Option option, OptionVisualOverride override) {
        setField(Option.class, option, "visualOverride", override);
    }

    public static Visual visual(RenderMode renderMode,
                                int outerDiameterPx,
                                int innerDiameterPx,
                                int labelRadiusPx,
                                int centerDiameterPx,
                                int borderThicknessPx,
                                int labelFontSize,
                                StatePalette statePalette,
                                String texturePrefix) {
        Visual visual = instantiate(Visual.class);
        Object geometry = instantiateByName("com.alechilles.radialmenu.config.RadialMenuConfig$Geometry");
        Object label = instantiateByName("com.alechilles.radialmenu.config.RadialMenuConfig$LabelVisual");
        TextureSet textureSet = instantiate(TextureSet.class);

        setField(geometry.getClass(), geometry, "outerDiameterPx", outerDiameterPx);
        setField(geometry.getClass(), geometry, "innerDiameterPx", innerDiameterPx);
        setField(geometry.getClass(), geometry, "labelRadiusPx", labelRadiusPx);
        setField(geometry.getClass(), geometry, "centerDiameterPx", centerDiameterPx);
        setField(label.getClass(), label, "fontSize", labelFontSize);
        setField(TextureSet.class, textureSet, "prefix", texturePrefix);

        setField(Visual.class, visual, "renderMode", renderMode);
        setField(Visual.class, visual, "geometry", geometry);
        setField(Visual.class, visual, "borderThicknessPx", borderThicknessPx);
        setField(Visual.class, visual, "label", label);
        setField(Visual.class, visual, "states", statePalette);
        setField(Visual.class, visual, "textureSet", textureSet);
        return visual;
    }

    public static OptionVisualOverride optionVisualOverride(Integer labelFontSize, StatePalette palette) {
        OptionVisualOverride override = instantiate(OptionVisualOverride.class);
        setField(OptionVisualOverride.class, override, "labelFontSize", labelFontSize);
        setField(OptionVisualOverride.class, override, "states", palette);
        return override;
    }

    public static StateColors stateColors(String fill, String text, String border) {
        StateColors stateColors = instantiate(StateColors.class);
        setField(StateColors.class, stateColors, "fillColor", fill);
        setField(StateColors.class, stateColors, "textColor", text);
        setField(StateColors.class, stateColors, "borderColor", border);
        return stateColors;
    }

    public static StatePalette statePalette(StateColors defaultState,
                                            StateColors hoverState,
                                            StateColors pressedState,
                                            StateColors selectedState,
                                            StateColors disabledState) {
        StatePalette statePalette = instantiate(StatePalette.class);
        setField(StatePalette.class, statePalette, "defaultState", defaultState);
        setField(StatePalette.class, statePalette, "hoverState", hoverState);
        setField(StatePalette.class, statePalette, "pressedState", pressedState);
        setField(StatePalette.class, statePalette, "selectedState", selectedState);
        setField(StatePalette.class, statePalette, "disabledState", disabledState);
        return statePalette;
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

    private static Object instantiateByName(String typeName) {
        try {
            Class<?> type = Class.forName(typeName);
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to instantiate " + typeName, ex);
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
