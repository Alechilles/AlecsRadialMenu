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
            .build();

    private static AssetStore<String, RadialMenuConfig, DefaultAssetMap<String, RadialMenuConfig>> ASSET_STORE;

    private AssetExtraInfo.Data data;
    private String key;
    private boolean enabled = true;
    private String[] itemIds = EMPTY_ITEM_IDS;
    private ExecutionMode executionMode = ExecutionMode.SelectAndArm;
    private String defaultOptionId;
    private Option[] options = EMPTY_OPTIONS;

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
