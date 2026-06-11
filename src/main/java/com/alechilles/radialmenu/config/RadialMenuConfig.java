package com.alechilles.radialmenu.config;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bson.BsonNull;
import org.bson.BsonValue;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.lookup.StringCodecMapCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.math.codec.Vector3dArrayCodec;
import org.joml.Vector3d;

public final class RadialMenuConfig implements JsonAssetWithMap<String, DefaultAssetMap<String, RadialMenuConfig>> {
    private static final Option[] EMPTY_OPTIONS = new Option[0];
    private static final String[] EMPTY_ITEM_IDS = new String[0];

    private static Codec<String> assetRefCodec(String assetType) {
        return new SilentCodec<>() {
            @Override
            public String decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo) {
                if (bsonValue == null || bsonValue.isNull()) {
                    return null;
                }
                try {
                    return Codec.STRING.decode(bsonValue, extraInfo);
                } catch (Exception ignored) {
                    return null;
                }
            }

            @Override
            public BsonValue encode(String value, ExtraInfo extraInfo) {
                if (value == null) {
                    return new BsonNull();
                }
                return Codec.STRING.encode(value, extraInfo);
            }

            @Nonnull
            @Override
            public Schema toSchema(@Nonnull SchemaContext context) {
                StringSchema schema = new StringSchema();
                schema.setHytaleAssetRef(assetType);
                return schema;
            }
        };
    }

    private static final Codec<String> SOUND_EVENT_CODEC = assetRefCodec("SoundEvent");
    private static final Codec<String> PARTICLE_SYSTEM_CODEC = assetRefCodec("ParticleSystem");
    private static final Codec<Vector3d> VECTOR3D_CODEC = new Vector3dArrayCodec();

    public static final BuilderCodec<Feedback> FEEDBACK_CODEC = BuilderCodec.builder(Feedback.class, Feedback::new)
            .<String>append(
                    new KeyedCodec<>("ChatMessage", Codec.STRING),
                    (feedback, value) -> feedback.chatMessage = value,
                    feedback -> feedback.chatMessage
            )
            .add()
            .<String>append(
                    new KeyedCodec<>("HudMessage", Codec.STRING),
                    (feedback, value) -> feedback.hudMessage = value,
                    feedback -> feedback.hudMessage
            )
            .add()
            .<String>append(
                    new KeyedCodec<>("SoundEvent", SOUND_EVENT_CODEC),
                    (feedback, value) -> feedback.soundEvent = value,
                    feedback -> feedback.soundEvent
            )
            .add()
            .<String>append(
                    new KeyedCodec<>("ParticleSystem", PARTICLE_SYSTEM_CODEC),
                    (feedback, value) -> feedback.particleSystem = value,
                    feedback -> feedback.particleSystem
            )
            .add()
            .<Vector3d>append(
                    new KeyedCodec<>("ParticleOffset", VECTOR3D_CODEC),
                    (feedback, value) -> feedback.particleOffset = value,
                    feedback -> feedback.particleOffset
            )
            .add()
            .build();

    public static final BuilderCodec<StateColors> STATE_COLORS_CODEC = BuilderCodec.builder(
                    StateColors.class,
                    StateColors::new
            )
            .<String>append(
                    new KeyedCodec<>("FillColor", Codec.STRING),
                    (colors, value) -> colors.fillColor = value,
                    colors -> colors.fillColor
            )
            .add()
            .<String>append(
                    new KeyedCodec<>("TextColor", Codec.STRING),
                    (colors, value) -> colors.textColor = value,
                    colors -> colors.textColor
            )
            .add()
            .<String>append(
                    new KeyedCodec<>("BorderColor", Codec.STRING),
                    (colors, value) -> colors.borderColor = value,
                    colors -> colors.borderColor
            )
            .add()
            .build();

    public static final BuilderCodec<StatePalette> STATE_PALETTE_CODEC = BuilderCodec.builder(
                    StatePalette.class,
                    StatePalette::new
            )
            .<StateColors>append(
                    new KeyedCodec<>("Default", STATE_COLORS_CODEC),
                    (palette, value) -> palette.defaultState = value,
                    palette -> palette.defaultState
            )
            .add()
            .<StateColors>append(
                    new KeyedCodec<>("Hover", STATE_COLORS_CODEC),
                    (palette, value) -> palette.hoverState = value,
                    palette -> palette.hoverState
            )
            .add()
            .<StateColors>append(
                    new KeyedCodec<>("Pressed", STATE_COLORS_CODEC),
                    (palette, value) -> palette.pressedState = value,
                    palette -> palette.pressedState
            )
            .add()
            .<StateColors>append(
                    new KeyedCodec<>("Selected", STATE_COLORS_CODEC),
                    (palette, value) -> palette.selectedState = value,
                    palette -> palette.selectedState
            )
            .add()
            .<StateColors>append(
                    new KeyedCodec<>("Disabled", STATE_COLORS_CODEC),
                    (palette, value) -> palette.disabledState = value,
                    palette -> palette.disabledState
            )
            .add()
            .build();

    public static final BuilderCodec<Geometry> GEOMETRY_CODEC = BuilderCodec.builder(Geometry.class, Geometry::new)
            .<Integer>append(
                    new KeyedCodec<>("OuterDiameterPx", Codec.INTEGER),
                    (geometry, value) -> geometry.outerDiameterPx = value == null ? Geometry.DEFAULT_OUTER_DIAMETER : value,
                    geometry -> geometry.outerDiameterPx
            )
            .add()
            .<Integer>append(
                    new KeyedCodec<>("InnerDiameterPx", Codec.INTEGER),
                    (geometry, value) -> geometry.innerDiameterPx = value == null ? Geometry.DEFAULT_INNER_DIAMETER : value,
                    geometry -> geometry.innerDiameterPx
            )
            .add()
            .<Integer>append(
                    new KeyedCodec<>("LabelRadiusPx", Codec.INTEGER),
                    (geometry, value) -> geometry.labelRadiusPx = value == null ? Geometry.DEFAULT_LABEL_RADIUS : value,
                    geometry -> geometry.labelRadiusPx
            )
            .add()
            .<Integer>append(
                    new KeyedCodec<>("CenterDiameterPx", Codec.INTEGER),
                    (geometry, value) -> geometry.centerDiameterPx = value == null ? Geometry.DEFAULT_CENTER_DIAMETER : value,
                    geometry -> geometry.centerDiameterPx
            )
            .add()
            .build();

    public static final BuilderCodec<LabelVisual> LABEL_VISUAL_CODEC = BuilderCodec.builder(
                    LabelVisual.class,
                    LabelVisual::new
            )
            .<Integer>append(
                    new KeyedCodec<>("FontSize", Codec.INTEGER),
                    (label, value) -> label.fontSize = value == null ? LabelVisual.DEFAULT_FONT_SIZE : value,
                    label -> label.fontSize
            )
            .add()
            .build();

    public static final BuilderCodec<TextureSet> TEXTURE_SET_CODEC = BuilderCodec.builder(
                    TextureSet.class,
                    TextureSet::new
            )
            .<String>append(
                    new KeyedCodec<>("Preset", Codec.STRING),
                    (textureSet, value) -> textureSet.preset = TexturePreset.fromString(value),
                    textureSet -> textureSet.getPreset().name()
            )
            .add()
            .<String>append(
                    new KeyedCodec<>("Prefix", Codec.STRING),
                    (textureSet, value) -> textureSet.prefix = value,
                    textureSet -> textureSet.prefix
            )
            .add()
            .build();

    public static final BuilderCodec<Visual> VISUAL_CODEC = BuilderCodec.builder(Visual.class, Visual::new)
            .<String>append(
                    new KeyedCodec<>("RenderMode", Codec.STRING),
                    (visual, value) -> visual.renderMode = RenderMode.fromString(value),
                    visual -> visual.getRenderMode().name()
            )
            .add()
            .<Geometry>append(
                    new KeyedCodec<>("Geometry", GEOMETRY_CODEC),
                    (visual, value) -> visual.geometry = value == null ? Geometry.defaults() : value,
                    visual -> visual.getGeometry()
            )
            .add()
            .<Integer>append(
                    new KeyedCodec<>("BorderThicknessPx", Codec.INTEGER),
                    (visual, value) -> visual.borderThicknessPx = value == null ? Visual.DEFAULT_BORDER_THICKNESS : value,
                    visual -> visual.borderThicknessPx
            )
            .add()
            .<LabelVisual>append(
                    new KeyedCodec<>("Label", LABEL_VISUAL_CODEC),
                    (visual, value) -> visual.label = value == null ? LabelVisual.defaults() : value,
                    visual -> visual.getLabel()
            )
            .add()
            .<StatePalette>append(
                    new KeyedCodec<>("States", STATE_PALETTE_CODEC),
                    (visual, value) -> visual.states = value == null ? StatePalette.defaults() : value,
                    visual -> visual.getStates()
            )
            .add()
            .<TextureSet>append(
                    new KeyedCodec<>("TextureSet", TEXTURE_SET_CODEC),
                    (visual, value) -> visual.textureSet = value == null ? TextureSet.defaults() : value,
                    visual -> visual.getTextureSet()
            )
            .add()
            .build();

    public static final BuilderCodec<OptionVisualOverride> OPTION_VISUAL_OVERRIDE_CODEC = BuilderCodec.builder(
                    OptionVisualOverride.class,
                    OptionVisualOverride::new
            )
            .<Integer>append(
                    new KeyedCodec<>("LabelFontSize", Codec.INTEGER),
                    (override, value) -> override.labelFontSize = value,
                    override -> override.labelFontSize
            )
            .add()
            .<StatePalette>append(
                    new KeyedCodec<>("States", STATE_PALETTE_CODEC),
                    (override, value) -> override.states = value,
                    override -> override.states
            )
            .add()
            .build();

    private static final BuilderCodec<Option> OPTION_BASE_CODEC = BuilderCodec.abstractBuilder(Option.class)
            .<String>append(
                    new KeyedCodec<>("Id", Codec.STRING),
                    (option, value) -> option.id = value,
                    option -> option.id
            )
            .add()
            .<String>append(
                    new KeyedCodec<>("LabelKey", Codec.STRING),
                    (option, value) -> option.labelKey = value,
                    option -> option.labelKey
            )
            .add()
            .<String>append(
                    new KeyedCodec<>("Label", Codec.STRING),
                    (option, value) -> option.label = value,
                    option -> option.label
            )
            .add()
            .<Feedback>append(
                    new KeyedCodec<>("Feedback", FEEDBACK_CODEC),
                    (option, value) -> option.feedback = value,
                    option -> option.feedback
            )
            .add()
            .<OptionVisualOverride>append(
                    new KeyedCodec<>("VisualOverride", OPTION_VISUAL_OVERRIDE_CODEC),
                    (option, value) -> option.visualOverride = value,
                    option -> option.visualOverride
            )
            .add()
            .build();

    private static final BuilderCodec<ExecuteCommandOption> EXECUTE_COMMAND_OPTION_CODEC =
            BuilderCodec.builder(ExecuteCommandOption.class, ExecuteCommandOption::new, OPTION_BASE_CODEC)
                    .<String>append(
                            new KeyedCodec<>("Command", Codec.STRING),
                            (option, value) -> option.command = value,
                            option -> option.command
                    )
                    .add()
                    .build();

    private static final BuilderCodec<InvokeRegisteredActionOption> INVOKE_REGISTERED_ACTION_OPTION_CODEC =
            BuilderCodec.builder(
                            InvokeRegisteredActionOption.class,
                            InvokeRegisteredActionOption::new,
                            OPTION_BASE_CODEC
                    )
                    .<String>append(
                            new KeyedCodec<>("ActionId", Codec.STRING),
                            (option, value) -> option.actionId = value,
                            option -> option.actionId
                    )
                    .add()
                    .<Map<String, String>>append(
                            new KeyedCodec<>("Payload", MapCodec.STRING_HASH_MAP_CODEC),
                            (option, value) -> option.payload = value == null ? Collections.emptyMap() : value,
                            option -> option.payload
                    )
                    .add()
                    .build();

    public static final StringCodecMapCodec<Option, BuilderCodec<? extends Option>> OPTION_CODEC =
            new StringCodecMapCodec<>("Type") {
            };

    static {
        OPTION_CODEC.register("ExecuteCommand", ExecuteCommandOption.class, EXECUTE_COMMAND_OPTION_CODEC);
        OPTION_CODEC.register(
                "InvokeRegisteredAction",
                InvokeRegisteredActionOption.class,
                INVOKE_REGISTERED_ACTION_OPTION_CODEC
        );
    }

    public static final ArrayCodec<Option> OPTION_ARRAY_CODEC = new ArrayCodec<>(OPTION_CODEC, Option[]::new);

    public static final AssetBuilderCodec<String, RadialMenuConfig> CODEC = AssetBuilderCodec.builder(
                    RadialMenuConfig.class,
                    RadialMenuConfig::new,
                    Codec.STRING,
                    (asset, id) -> asset.key = id,
                    asset -> asset.key,
                    (asset, data) -> asset.data = data,
                    asset -> asset.data
            )
            .documentation("Asset-driven radial menu definition.")
            .<Boolean>append(
                    new KeyedCodec<>("Enabled", Codec.BOOLEAN),
                    (asset, value) -> asset.enabled = value == null || value,
                    asset -> asset.enabled
            )
            .add()
            .<String[]>append(
                    new KeyedCodec<>("ItemIds", Codec.STRING_ARRAY),
                    (asset, value) -> asset.itemIds = value == null ? EMPTY_ITEM_IDS : value,
                    asset -> asset.itemIds
            )
            .add()
            .<String>append(
                    new KeyedCodec<>("ExecutionMode", Codec.STRING),
                    (asset, value) -> asset.executionMode = ExecutionMode.fromString(value),
                    asset -> asset.executionMode.name()
            )
            .add()
            .<String>append(
                    new KeyedCodec<>("DefaultOptionId", Codec.STRING),
                    (asset, value) -> asset.defaultOptionId = value,
                    asset -> asset.defaultOptionId
            )
            .add()
            .<Option[]>append(
                    new KeyedCodec<>("Options", OPTION_ARRAY_CODEC),
                    (asset, value) -> asset.options = value == null ? EMPTY_OPTIONS : value,
                    asset -> asset.options
            )
            .add()
            .<Visual>append(
                    new KeyedCodec<>("Visual", VISUAL_CODEC),
                    (asset, value) -> asset.visual = value == null ? Visual.defaults() : value,
                    asset -> asset.getVisual()
            )
            .add()
            .build();

    private static AssetStore<String, RadialMenuConfig, DefaultAssetMap<String, RadialMenuConfig>> ASSET_STORE;

    private AssetExtraInfo.Data data;
    private String key;
    private boolean enabled = true;
    private String[] itemIds = EMPTY_ITEM_IDS;
    private ExecutionMode executionMode = ExecutionMode.SelectAndArm;
    private String defaultOptionId;
    private Option[] options = EMPTY_OPTIONS;
    private Visual visual = Visual.defaults();

    private RadialMenuConfig() {
    }

    public static AssetStore<String, RadialMenuConfig, DefaultAssetMap<String, RadialMenuConfig>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(RadialMenuConfig.class);
        }
        return ASSET_STORE;
    }

    @Nullable
    public static DefaultAssetMap<String, RadialMenuConfig> getAssetMap() {
        AssetStore<String, RadialMenuConfig, DefaultAssetMap<String, RadialMenuConfig>> store = getAssetStore();
        if (store == null) {
            return null;
        }
        return (DefaultAssetMap<String, RadialMenuConfig>) store.getAssetMap();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String[] getItemIds() {
        return itemIds;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode != null ? executionMode : ExecutionMode.SelectAndArm;
    }

    @Nullable
    public String getDefaultOptionId() {
        return defaultOptionId;
    }

    public Option[] getOptions() {
        return options;
    }

    @Nonnull
    public Visual getVisual() {
        return visual == null ? Visual.defaults() : visual;
    }

    @Nullable
    public String getKey() {
        return key;
    }

    @Nullable
    @Override
    public String getId() {
        return key;
    }

    @Nullable
    public Option findOptionById(@Nullable String optionId) {
        String normalized = normalize(optionId);
        if (normalized == null || options == null) {
            return null;
        }
        for (Option option : options) {
            String current = normalize(option == null ? null : option.id);
            if (normalized.equals(current)) {
                return option;
            }
        }
        return null;
    }

    @Nullable
    public Option resolveDefaultOption() {
        Option explicit = findOptionById(defaultOptionId);
        if (explicit != null) {
            return explicit;
        }
        if (options == null || options.length == 0) {
            return null;
        }
        for (Option option : options) {
            if (normalize(option == null ? null : option.id) != null) {
                return option;
            }
        }
        return null;
    }

    @Nullable
    private static String normalize(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public enum ExecutionMode {
        SelectAndArm,
        SelectAndRun;

        @Nonnull
        public static ExecutionMode fromString(@Nullable String value) {
            if (value == null || value.isBlank()) {
                return SelectAndArm;
            }
            for (ExecutionMode mode : values()) {
                if (mode.name().equalsIgnoreCase(value.trim())) {
                    return mode;
                }
            }
            return SelectAndArm;
        }
    }

    public abstract static class Option {
        private String id;
        private String labelKey;
        private String label;
        private Feedback feedback;
        private OptionVisualOverride visualOverride;

        @Nullable
        public String getId() {
            return id;
        }

        @Nullable
        public String getLabelKey() {
            return labelKey;
        }

        @Nullable
        public String getLabel() {
            return label;
        }

        @Nullable
        public Feedback getFeedback() {
            return feedback;
        }

        @Nullable
        public OptionVisualOverride getVisualOverride() {
            return visualOverride;
        }
    }

    public static final class ExecuteCommandOption extends Option {
        private String command;

        @Nullable
        public String getCommand() {
            return command;
        }
    }

    public static final class InvokeRegisteredActionOption extends Option {
        private String actionId;
        private Map<String, String> payload = Collections.emptyMap();

        @Nullable
        public String getActionId() {
            return actionId;
        }

        @Nonnull
        public Map<String, String> getPayload() {
            return payload;
        }
    }

    public static final class Feedback {
        private String chatMessage;
        private String hudMessage;
        private String soundEvent;
        private String particleSystem;
        private Vector3d particleOffset;

        @Nullable
        public String getChatMessage() {
            return chatMessage;
        }

        @Nullable
        public String getHudMessage() {
            return hudMessage;
        }

        @Nullable
        public String getSoundEvent() {
            return soundEvent;
        }

        @Nullable
        public String getParticleSystem() {
            return particleSystem;
        }

        @Nullable
        public Vector3d getParticleOffset() {
            return particleOffset;
        }
    }

    public enum RenderMode {
        Vector,
        Texture;

        @Nonnull
        public static RenderMode fromString(@Nullable String value) {
            if (value == null || value.isBlank()) {
                return Vector;
            }
            for (RenderMode mode : values()) {
                if (mode.name().equalsIgnoreCase(value.trim())) {
                    return mode;
                }
            }
            return Vector;
        }
    }

    public enum TexturePreset {
        LegacyDefault;

        @Nonnull
        public static TexturePreset fromString(@Nullable String value) {
            if (value == null || value.isBlank()) {
                return LegacyDefault;
            }
            for (TexturePreset preset : values()) {
                if (preset.name().equalsIgnoreCase(value.trim())) {
                    return preset;
                }
            }
            return LegacyDefault;
        }
    }

    public static final class Geometry {
        public static final int DEFAULT_OUTER_DIAMETER = 640;
        public static final int DEFAULT_INNER_DIAMETER = 300;
        public static final int DEFAULT_LABEL_RADIUS = 234;
        public static final int DEFAULT_CENTER_DIAMETER = 300;

        private int outerDiameterPx = DEFAULT_OUTER_DIAMETER;
        private int innerDiameterPx = DEFAULT_INNER_DIAMETER;
        private int labelRadiusPx = DEFAULT_LABEL_RADIUS;
        private int centerDiameterPx = DEFAULT_CENTER_DIAMETER;

        private Geometry() {
        }

        @Nonnull
        public static Geometry defaults() {
            return new Geometry();
        }

        public int getOuterDiameterPx() {
            return outerDiameterPx;
        }

        public int getInnerDiameterPx() {
            return innerDiameterPx;
        }

        public int getLabelRadiusPx() {
            return labelRadiusPx;
        }

        public int getCenterDiameterPx() {
            return centerDiameterPx;
        }
    }

    public static final class LabelVisual {
        public static final int DEFAULT_FONT_SIZE = 15;

        private int fontSize = DEFAULT_FONT_SIZE;

        private LabelVisual() {
        }

        @Nonnull
        public static LabelVisual defaults() {
            return new LabelVisual();
        }

        public int getFontSize() {
            return fontSize;
        }
    }

    public static final class StateColors {
        private String fillColor;
        private String textColor;
        private String borderColor;

        private StateColors() {
        }

        private StateColors(String fillColor, String textColor, String borderColor) {
            this.fillColor = fillColor;
            this.textColor = textColor;
            this.borderColor = borderColor;
        }

        @Nonnull
        public static StateColors of(@Nonnull String fillColor, @Nonnull String textColor, @Nonnull String borderColor) {
            return new StateColors(fillColor, textColor, borderColor);
        }

        @Nonnull
        public static StateColors merge(@Nonnull StateColors base, @Nullable StateColors override) {
            if (override == null) {
                return new StateColors(base.fillColor, base.textColor, base.borderColor);
            }
            return new StateColors(
                    override.fillColor == null || override.fillColor.isBlank() ? base.fillColor : override.fillColor,
                    override.textColor == null || override.textColor.isBlank() ? base.textColor : override.textColor,
                    override.borderColor == null || override.borderColor.isBlank() ? base.borderColor : override.borderColor
            );
        }

        @Nullable
        public String getFillColor() {
            return fillColor;
        }

        @Nullable
        public String getTextColor() {
            return textColor;
        }

        @Nullable
        public String getBorderColor() {
            return borderColor;
        }
    }

    public static final class StatePalette {
        private StateColors defaultState;
        private StateColors hoverState;
        private StateColors pressedState;
        private StateColors selectedState;
        private StateColors disabledState;

        private StatePalette() {
        }

        private StatePalette(StateColors defaultState,
                             StateColors hoverState,
                             StateColors pressedState,
                             StateColors selectedState,
                             StateColors disabledState) {
            this.defaultState = defaultState;
            this.hoverState = hoverState;
            this.pressedState = pressedState;
            this.selectedState = selectedState;
            this.disabledState = disabledState;
        }

        @Nonnull
        public static StatePalette defaults() {
            return new StatePalette(
                    StateColors.of("#3b5263", "#d6e0ec", "#1b2730"),
                    StateColors.of("#4f7089", "#ffffff", "#2c4252"),
                    StateColors.of("#2d4151", "#ffffff", "#17232c"),
                    StateColors.of("#5d829f", "#ffffff", "#30495b"),
                    StateColors.of("#26343f(0.70)", "#9eafbf(0.85)", "#182127(0.70)")
            );
        }

        @Nonnull
        public static StatePalette withDefaults(@Nullable StatePalette palette) {
            StatePalette defaults = defaults();
            if (palette == null) {
                return defaults;
            }
            return new StatePalette(
                    StateColors.merge(defaults.defaultState, palette.defaultState),
                    StateColors.merge(defaults.hoverState, palette.hoverState),
                    StateColors.merge(defaults.pressedState, palette.pressedState),
                    StateColors.merge(defaults.selectedState, palette.selectedState),
                    StateColors.merge(defaults.disabledState, palette.disabledState)
            );
        }

        @Nullable
        public StateColors getDefaultStateRaw() {
            return defaultState;
        }

        @Nullable
        public StateColors getHoverStateRaw() {
            return hoverState;
        }

        @Nullable
        public StateColors getPressedStateRaw() {
            return pressedState;
        }

        @Nullable
        public StateColors getSelectedStateRaw() {
            return selectedState;
        }

        @Nullable
        public StateColors getDisabledStateRaw() {
            return disabledState;
        }

        @Nonnull
        public StateColors getDefaultState() {
            return StateColors.merge(defaults().defaultState, defaultState);
        }

        @Nonnull
        public StateColors getHoverState() {
            return StateColors.merge(defaults().hoverState, hoverState);
        }

        @Nonnull
        public StateColors getPressedState() {
            return StateColors.merge(defaults().pressedState, pressedState);
        }

        @Nonnull
        public StateColors getSelectedState() {
            return StateColors.merge(defaults().selectedState, selectedState);
        }

        @Nonnull
        public StateColors getDisabledState() {
            return StateColors.merge(defaults().disabledState, disabledState);
        }
    }

    public static final class TextureSet {
        private TexturePreset preset = TexturePreset.LegacyDefault;
        private String prefix;

        private TextureSet() {
        }

        @Nonnull
        public static TextureSet defaults() {
            return new TextureSet();
        }

        @Nonnull
        public TexturePreset getPreset() {
            return preset == null ? TexturePreset.LegacyDefault : preset;
        }

        @Nullable
        public String getPrefix() {
            return prefix;
        }
    }

    public static final class Visual {
        public static final int DEFAULT_BORDER_THICKNESS = 2;

        private RenderMode renderMode = RenderMode.Vector;
        private Geometry geometry = Geometry.defaults();
        private int borderThicknessPx = DEFAULT_BORDER_THICKNESS;
        private LabelVisual label = LabelVisual.defaults();
        private StatePalette states = StatePalette.defaults();
        private TextureSet textureSet = TextureSet.defaults();

        private Visual() {
        }

        @Nonnull
        public static Visual defaults() {
            return new Visual();
        }

        @Nonnull
        public RenderMode getRenderMode() {
            return renderMode == null ? RenderMode.Vector : renderMode;
        }

        @Nonnull
        public Geometry getGeometry() {
            return geometry == null ? Geometry.defaults() : geometry;
        }

        public int getBorderThicknessPx() {
            return borderThicknessPx;
        }

        @Nonnull
        public LabelVisual getLabel() {
            return label == null ? LabelVisual.defaults() : label;
        }

        @Nonnull
        public StatePalette getStates() {
            return StatePalette.withDefaults(states);
        }

        @Nullable
        public StatePalette getStatesRaw() {
            return states;
        }

        @Nonnull
        public TextureSet getTextureSet() {
            return textureSet == null ? TextureSet.defaults() : textureSet;
        }
    }

    public static final class OptionVisualOverride {
        private Integer labelFontSize;
        private StatePalette states;

        private OptionVisualOverride() {
        }

        @Nullable
        public Integer getLabelFontSize() {
            return labelFontSize;
        }

        @Nullable
        public StatePalette getStates() {
            return states;
        }
    }

    private abstract static class SilentCodec<T> implements Codec<T> {
        @Override
        public abstract T decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo);

        @Override
        public abstract BsonValue encode(T value, ExtraInfo extraInfo);

        @Nonnull
        @Override
        public abstract Schema toSchema(@Nonnull SchemaContext context);
    }
}
