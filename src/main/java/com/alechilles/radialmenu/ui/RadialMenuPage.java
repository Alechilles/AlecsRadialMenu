package com.alechilles.radialmenu.ui;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.config.RadialMenuConfig.Option;
import com.alechilles.radialmenu.config.RadialMenuConfig.RenderMode;
import com.alechilles.radialmenu.localization.RadialMenuLocalizedText;
import com.alechilles.radialmenu.ui.RadialMenuVisualResolver.ResolvedOptionVisual;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class RadialMenuPage extends InteractiveCustomUIPage<RadialMenuPage.RadialMenuEventData> {
    public static final String UI_PATH = "RadialMenu.ui";
    private static final String VECTOR_TEXTURE_PREFIX = "RadialMenu/Vector";

    private static final String EVENT_OPTION_ID = "OptionId";
    private static final String EVENT_ACTION = "Action";
    private static final String ACTION_SELECT = "Select";
    private static final String CLOSE_OPTION_ID = "__close__";
    private static final int MAX_OPTIONS = 8;
    /**
     * Mapping from logical option slot index (legacy wheel order) to texture file index
     * for the new exported custom-slice layout.
     */
    private static final int[] NEW_TEXTURE_INDEX_BY_OPTION = new int[] {6, 7, 0, 1, 2, 3, 4, 5};
    private static final double CUSTOM_TEXTURE_LAYOUT_ANGLE_OFFSET_DEGREES = 29.0;
    static final double TEXTURE_LABEL_ANGLE_OFFSET_DEGREES = 22.5;
    private static final double CUSTOM_TEXTURE_SLOT_RADIUS = 260.0;
    private static final double CUSTOM_TEXTURE_CENTER_OFFSET_X = -6.0;
    private static final double CUSTOM_TEXTURE_CENTER_OFFSET_Y = -3.0;

    private static final int BASE_CENTER_X = 460;
    private static final int BASE_CENTER_Y = 420;
    private static final int BASE_OUTER_DIAMETER = 640;

    private static final Rect[] BASE_BUTTON_RECTS = new Rect[] {
            new Rect(121, 352, 217, 142),
            new Rect(147, 526, 207, 207),
            new Rect(312, 617, 142, 217),
            new Rect(486, 526, 207, 207),
            new Rect(577, 352, 217, 142),
            new Rect(486, 187, 207, 207),
            new Rect(312, 161, 142, 217),
            new Rect(147, 187, 207, 207)
    };

    private static final Rect[] BASE_LABEL_RECTS = new Rect[] {
            new Rect(164, 376, 168, 44),
            new Rect(233, 541, 170, 44),
            new Rect(398, 610, 170, 44),
            new Rect(563, 541, 170, 44),
            new Rect(632, 376, 168, 44),
            new Rect(563, 211, 170, 44),
            new Rect(398, 142, 170, 44),
            new Rect(233, 211, 170, 44)
    };

    private final String menuKey;
    private final RadialMenuConfig config;
    private final DisplayOption[] options;
    private final String selectedOptionId;
    private final Consumer<String> selectionCallback;
    private final Map<String, TextureMetrics> textureMetricsCache;
    @Nullable
    private final HytaleLogger logger;
    private boolean handled;

    public RadialMenuPage(@Nonnull PlayerRef playerRef,
                          @Nonnull String menuKey,
                          @Nonnull RadialMenuConfig config,
                          @Nullable String selectedOptionId,
                          @Nonnull Consumer<String> selectionCallback,
                          @Nullable HytaleLogger logger) {
        super(playerRef, CustomPageLifetime.CanDismiss, RadialMenuEventData.CODEC);
        this.menuKey = menuKey;
        this.config = config;
        this.options = buildOptions(playerRef, config);
        this.selectedOptionId = selectedOptionId;
        this.selectionCallback = selectionCallback;
        this.textureMetricsCache = new HashMap<>();
        this.logger = logger;
        this.handled = false;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder commandBuilder,
                      @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store<EntityStore> store) {
        commandBuilder.append(UI_PATH);
        commandBuilder.set("#RadialMenuWheel.Visible", true);
        commandBuilder.set("#RadialMenuTitle.Text", RadialMenuLocalizedText.resolve(playerRef, "radialmenu.ui.title"));
        commandBuilder.set("#RadialMenuSubtitle.Text", RadialMenuLocalizedText.resolve(playerRef, "radialmenu.ui.subtitle"));
        commandBuilder.set("#RadialMenuCurrent.Text", resolveCurrentLabel());

        RadialMenuConfig.Visual visual = config.getVisual();
        RenderMode renderMode = visual.getRenderMode();
        boolean vectorMode = renderMode == RenderMode.Vector;
        String texturePrefix = resolveTexturePrefix(config);
        boolean fullWheelTextureMode = !vectorMode && isFullWheelTextureSet(texturePrefix);

        applyGeometry(commandBuilder, visual.getGeometry());
        applyRingVisuals(commandBuilder, texturePrefix, visual, vectorMode);

        for (int i = 0; i < MAX_OPTIONS; i++) {
            String buttonSelector = "#CommandButton" + i;
            String visualSelector = "#CommandVisual" + i;
            String borderSelector = "#CommandBorder" + i;
            String labelSelector = "#CommandLabel" + i;
            if (i >= options.length) {
                commandBuilder.set(visualSelector + ".Visible", false);
                commandBuilder.set(buttonSelector + ".Visible", false);
                commandBuilder.set(borderSelector + ".Visible", false);
                commandBuilder.set(labelSelector + ".Visible", false);
                continue;
            }

            DisplayOption option = options[i];
            commandBuilder.set(visualSelector + ".Visible", !vectorMode && !fullWheelTextureMode);
            commandBuilder.set(buttonSelector + ".Visible", true);
            commandBuilder.set(borderSelector + ".Visible", vectorMode);
            commandBuilder.set(buttonSelector + ".Text", "");
            commandBuilder.set(labelSelector + ".Visible", true);
            commandBuilder.set(labelSelector + ".Text", option.label());

            applyLabelGeometry(commandBuilder, labelSelector, visual.getGeometry(), i, texturePrefix, vectorMode);
            applyButtonGeometry(
                    commandBuilder,
                    buttonSelector,
                    borderSelector,
                    visual.getGeometry(),
                    i,
                    texturePrefix,
                    vectorMode,
                    Math.max(0, visual.getBorderThicknessPx())
            );
            applyButtonVisuals(
                    commandBuilder,
                    visualSelector,
                    buttonSelector,
                    borderSelector,
                    labelSelector,
                    texturePrefix,
                    option,
                    i,
                    vectorMode
            );

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    buttonSelector,
                    EventData.of(EVENT_OPTION_ID, option.id()).append(EVENT_ACTION, ACTION_SELECT),
                    false
            );
        }

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#RadialMenuCloseButton",
                EventData.of(EVENT_OPTION_ID, CLOSE_OPTION_ID).append(EVENT_ACTION, ACTION_SELECT),
                false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull RadialMenuEventData data) {
        if (data.optionId == null || data.optionId.isBlank()) {
            return;
        }
        if (CLOSE_OPTION_ID.equalsIgnoreCase(data.optionId)) {
            handled = true;
            close();
            return;
        }
        DisplayOption selected = findOption(data.optionId);
        if (selected == null) {
            handled = true;
            close();
            return;
        }
        handled = true;
        close();
        selectionCallback.accept(selected.id());
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        handled = true;
    }

    private void applyRingVisuals(@Nonnull UICommandBuilder commandBuilder,
                                  @Nonnull String texturePrefix,
                                  @Nonnull RadialMenuConfig.Visual visual,
                                  boolean vectorMode) {
        // Legacy outer/inner wheel rings are intentionally disabled for the standalone radial menu.
        commandBuilder.set("#CommandWheelOuterRing.Visible", false);
        commandBuilder.set("#CommandWheelInnerRing.Visible", false);
        commandBuilder.set("#CommandWheelCenterPanel.Visible", true);
        commandBuilder.set("#CommandWheelCenterBorder.Visible", vectorMode);

        String centerTexture = vectorMode
                ? texturePrefix + "/CommandWheelCenterPanel_Fill.png"
                : texturePrefix + "/CommandWheelCenterPanel.png";
        String centerFillColor = vectorMode ? visual.getStates().getDefaultState().getFillColor() : null;
        commandBuilder.setObject(
                "#CommandWheelCenterPanel.Background",
                buildPatchStyle(centerTexture, centerFillColor)
        );
        if (vectorMode) {
            commandBuilder.setObject(
                    "#CommandWheelCenterBorder.Background",
                    buildPatchStyle(
                            texturePrefix + "/CommandWheelCenterPanel_Border.png",
                            visual.getStates().getDefaultState().getBorderColor()
                    )
            );
        }
        if (vectorMode) {
            commandBuilder.set("#RadialMenuCurrent.Style.TextColor", visual.getStates().getDefaultState().getTextColor());
            commandBuilder.set("#RadialMenuCurrent.Style.OutlineColor", visual.getStates().getDefaultState().getBorderColor());
        }
    }

    private void applyButtonVisuals(@Nonnull UICommandBuilder commandBuilder,
                                    @Nonnull String visualSelector,
                                    @Nonnull String buttonSelector,
                                    @Nonnull String borderSelector,
                                    @Nonnull String labelSelector,
                                    @Nonnull String texturePrefix,
                                    @Nonnull DisplayOption displayOption,
                                    int optionIndex,
                                    boolean vectorMode) {
        Option option = findOptionConfig(displayOption.id());
        if (option == null) {
            option = firstOption();
        }
        if (option == null) {
            return;
        }

        boolean isSelected = selectedOptionId != null && selectedOptionId.equalsIgnoreCase(displayOption.id());
        ResolvedOptionVisual style = RadialMenuVisualResolver.resolveOptionVisual(config, option, isSelected);
        int textureIndex = vectorMode ? optionIndex : resolveTextureIndex(texturePrefix, optionIndex);

        String defaultTexture = vectorMode
                ? texturePrefix + "/CommandWheelSlice" + textureIndex + "_Fill.png"
                : texturePrefix + "/CommandWheelSlice" + textureIndex + "_Default.png";
        String hoverTexture = vectorMode
                ? defaultTexture
                : texturePrefix + "/CommandWheelSlice" + textureIndex + "_Hover.png";
        String pressedTexture = vectorMode
                ? defaultTexture
                : texturePrefix + "/CommandWheelSlice" + textureIndex + "_Pressed.png";
        String defaultColor = vectorMode ? style.defaultState().fillColor() : null;
        String hoverColor = vectorMode ? style.hoverState().fillColor() : null;
        String pressedColor = vectorMode ? style.pressedState().fillColor() : null;

        if (!vectorMode && isFullWheelTextureSet(texturePrefix)) {
            String croppedTexturePrefix = texturePrefix + "/Cropped";
            defaultTexture = croppedTexturePrefix + "/CommandWheelSlice" + textureIndex + "_Default.png";
            hoverTexture = croppedTexturePrefix + "/CommandWheelSlice" + textureIndex + "_Hover.png";
            pressedTexture = croppedTexturePrefix + "/CommandWheelSlice" + textureIndex + "_Pressed.png";
            commandBuilder.setObject(
                    buttonSelector + ".Style.Default.Background",
                    buildPatchStyle(defaultTexture, null)
            );
            commandBuilder.setObject(
                    buttonSelector + ".Style.Hovered.Background",
                    buildPatchStyle(hoverTexture, null)
            );
            commandBuilder.setObject(
                    buttonSelector + ".Style.Pressed.Background",
                    buildPatchStyle(pressedTexture, null)
            );
        } else {
            commandBuilder.setObject(
                    buttonSelector + ".Style.Default.Background",
                    buildPatchStyle(defaultTexture, defaultColor)
            );
            commandBuilder.setObject(
                    buttonSelector + ".Style.Hovered.Background",
                    buildPatchStyle(hoverTexture, hoverColor)
            );
            commandBuilder.setObject(
                    buttonSelector + ".Style.Pressed.Background",
                    buildPatchStyle(pressedTexture, pressedColor)
            );
        }

        if (vectorMode) {
            commandBuilder.setObject(
                    borderSelector + ".Background",
                    buildPatchStyle(
                            texturePrefix + "/CommandWheelSlice" + optionIndex + "_Border.png",
                            style.defaultState().borderColor()
                    )
            );
        }

        commandBuilder.set(labelSelector + ".Style.FontSize", style.labelFontSize());
        commandBuilder.set(labelSelector + ".Style.TextColor", style.defaultState().textColor());
        commandBuilder.set(labelSelector + ".Style.OutlineColor", style.defaultState().borderColor());
    }

    private void applyTransparentButtonBackground(@Nonnull UICommandBuilder commandBuilder,
                                                  @Nonnull String buttonSelector) {
        PatchStyle transparent = new PatchStyle();
        transparent.setColor(Value.of("#00000000"));
        commandBuilder.setObject(buttonSelector + ".Style.Default.Background", transparent);
        commandBuilder.setObject(buttonSelector + ".Style.Hovered.Background", transparent);
        commandBuilder.setObject(buttonSelector + ".Style.Pressed.Background", transparent);
    }

    private void applyGeometry(@Nonnull UICommandBuilder commandBuilder, @Nonnull RadialMenuConfig.Geometry geometry) {
        int outerDiameter = safePositive(geometry.getOuterDiameterPx(), RadialMenuConfig.Geometry.DEFAULT_OUTER_DIAMETER);
        int innerDiameter = safePositive(geometry.getInnerDiameterPx(), RadialMenuConfig.Geometry.DEFAULT_INNER_DIAMETER);
        int centerDiameter = safePositive(geometry.getCenterDiameterPx(), RadialMenuConfig.Geometry.DEFAULT_CENTER_DIAMETER);

        Rect outerRect = centeredRect(BASE_CENTER_X, BASE_CENTER_Y, outerDiameter, outerDiameter);
        Rect innerRect = centeredRect(BASE_CENTER_X, BASE_CENTER_Y, innerDiameter, innerDiameter);
        Rect centerRect = centeredRect(BASE_CENTER_X, BASE_CENTER_Y, centerDiameter, centerDiameter);

        commandBuilder.setObject("#CommandWheelOuterRing.Anchor", outerRect.toAnchorObject());
        commandBuilder.setObject("#CommandWheelInnerRing.Anchor", innerRect.toAnchorObject());
        commandBuilder.setObject("#CommandWheelCenterPanel.Anchor", centerRect.toAnchorObject());
        commandBuilder.setObject("#CommandWheelCenterBorder.Anchor", centerRect.toAnchorObject());
        commandBuilder.setObject("#RadialMenuCurrent.Anchor", centerTextRect(centerRect).toAnchorObject());
        for (int i = 0; i < MAX_OPTIONS; i++) {
            commandBuilder.setObject("#CommandVisual" + i + ".Anchor", outerRect.toAnchorObject());
        }
    }

    private void applyButtonGeometry(@Nonnull UICommandBuilder commandBuilder,
                                     @Nonnull String buttonSelector,
                                     @Nonnull String borderSelector,
                                     @Nonnull RadialMenuConfig.Geometry geometry,
                                     int optionIndex,
                                     @Nonnull String texturePrefix,
                                     boolean vectorMode,
                                     int borderThicknessPx) {
        double scale = safePositive(geometry.getOuterDiameterPx(), RadialMenuConfig.Geometry.DEFAULT_OUTER_DIAMETER)
                / (double) BASE_OUTER_DIAMETER;
        Rect base = BASE_BUTTON_RECTS[optionIndex];
        if (!vectorMode) {
            Rect hitRect;
            if (isFullWheelTextureSet(texturePrefix)) {
                hitRect = resolveFullWheelHitRect(texturePrefix, optionIndex, scale, base);
            } else {
                hitRect = resolveTextureModeRect(texturePrefix, optionIndex, scale, base);
            }
            commandBuilder.setObject(buttonSelector + ".Anchor", hitRect.toAnchorObject());
            commandBuilder.setObject(borderSelector + ".Anchor", hitRect.toAnchorObject());
            return;
        }

        Rect scaled = base.scaleAround(BASE_CENTER_X, BASE_CENTER_Y, scale);
        if (vectorMode && borderThicknessPx > 0) {
            int maxInset = Math.max(0, Math.min((scaled.width - 1) / 4, (scaled.height - 1) / 4));
            int inset = Math.min(borderThicknessPx, maxInset);
            if (inset > 0) {
                scaled = scaled.inset(inset);
            }
        }
        commandBuilder.setObject(buttonSelector + ".Anchor", scaled.toAnchorObject());
        commandBuilder.setObject(borderSelector + ".Anchor", scaled.toAnchorObject());
    }

    @Nonnull
    private Rect resolveFullWheelHitRect(@Nonnull String texturePrefix,
                                         int optionIndex,
                                         double scale,
                                         @Nonnull Rect fallbackBaseRect) {
        int textureIndex = resolveTextureIndex(texturePrefix, optionIndex);
        TextureMetrics metrics = resolveTextureMetrics(texturePrefix, textureIndex);
        if (metrics == null || !metrics.hasAlphaBounds()) {
            return fallbackBaseRect.scaleAround(BASE_CENTER_X, BASE_CENTER_Y, scale);
        }

        Rect outerRect = centeredRect(
                BASE_CENTER_X,
                BASE_CENTER_Y,
                (int) Math.round(BASE_OUTER_DIAMETER * scale),
                (int) Math.round(BASE_OUTER_DIAMETER * scale)
        );
        int left = outerRect.left() + (int) Math.round(metrics.alphaLeft() * scale);
        int top = outerRect.top() + (int) Math.round(metrics.alphaTop() * scale);
        int width = Math.max(1, (int) Math.round((metrics.alphaRight() - metrics.alphaLeft()) * scale));
        int height = Math.max(1, (int) Math.round((metrics.alphaBottom() - metrics.alphaTop()) * scale));
        return new Rect(top, left, width, height);
    }

    @Nonnull
    private Rect resolveTextureModeRect(@Nonnull String texturePrefix,
                                        int optionIndex,
                                        double scale,
                                        @Nonnull Rect fallbackBaseRect) {
        int textureIndex = resolveTextureIndex(texturePrefix, optionIndex);
        TextureMetrics metrics = resolveTextureMetrics(texturePrefix, textureIndex);
        if (metrics == null) {
            return fallbackBaseRect.scaleAround(BASE_CENTER_X, BASE_CENTER_Y, scale);
        }
        int width = Math.max(1, (int) Math.round(metrics.width() * scale));
        int height = Math.max(1, (int) Math.round(metrics.height() * scale));
        Point textureCenter = resolveTextureAnchorCenter(texturePrefix, fallbackBaseRect, optionIndex, scale);
        if (isLegacyTexturePrefix(texturePrefix)) {
            return centeredRect(
                    (int) Math.round(textureCenter.x()),
                    (int) Math.round(textureCenter.y()),
                    width,
                    height
            );
        }
        return centeredRect(
                (int) Math.round(textureCenter.x()),
                (int) Math.round(textureCenter.y()),
                width,
                height
        );
    }

    @Nonnull
    private Point resolveTextureAnchorCenter(@Nonnull String texturePrefix,
                                             @Nonnull Rect fallbackBaseRect,
                                             int optionIndex,
                                             double scale) {
        int fallbackCenterX = fallbackBaseRect.centerX();
        int fallbackCenterY = fallbackBaseRect.centerY();
        if (isLegacyTexturePrefix(texturePrefix)) {
            double scaledX = BASE_CENTER_X + (fallbackCenterX - BASE_CENTER_X) * scale;
            double scaledY = BASE_CENTER_Y + (fallbackCenterY - BASE_CENTER_Y) * scale;
            return new Point(scaledX, scaledY);
        }
        double angleDegrees = -90.0 + optionIndex * 45.0 + CUSTOM_TEXTURE_LAYOUT_ANGLE_OFFSET_DEGREES;
        double radians = Math.toRadians(angleDegrees);
        double radius = CUSTOM_TEXTURE_SLOT_RADIUS * scale;
        double centerX = BASE_CENTER_X + (CUSTOM_TEXTURE_CENTER_OFFSET_X * scale) + Math.cos(radians) * radius;
        double centerY = BASE_CENTER_Y + (CUSTOM_TEXTURE_CENTER_OFFSET_Y * scale) + Math.sin(radians) * radius;
        return new Point(centerX, centerY);
    }

    private boolean isLegacyTexturePrefix(@Nonnull String texturePrefix) {
        return RadialMenuVisualResolver.LEGACY_TEXTURE_PREFIX.equalsIgnoreCase(texturePrefix);
    }

    private boolean isFullWheelTextureSet(@Nonnull String texturePrefix) {
        TextureMetrics metrics = resolveTextureMetrics(texturePrefix, 0);
        return metrics != null
                && metrics.width() == BASE_OUTER_DIAMETER
                && metrics.height() == BASE_OUTER_DIAMETER;
    }

    private int resolveTextureIndex(@Nonnull String texturePrefix, int optionIndex) {
        if (optionIndex < 0 || optionIndex >= MAX_OPTIONS) {
            return optionIndex;
        }
        if (isLegacyTexturePrefix(texturePrefix)) {
            return optionIndex;
        }
        return NEW_TEXTURE_INDEX_BY_OPTION[optionIndex];
    }

    @Nullable
    private TextureMetrics resolveTextureMetrics(@Nonnull String texturePrefix, int textureIndex) {
        String texturePath = texturePrefix + "/CommandWheelSlice" + textureIndex + "_Default.png";
        String normalized = texturePath.replace('\\', '/');
        TextureMetrics cached = textureMetricsCache.get(normalized);
        if (cached != null) {
            return cached.known() ? cached : null;
        }
        TextureMetrics loaded = loadTextureMetricsFromResource(normalized);
        textureMetricsCache.put(normalized, loaded == null ? TextureMetrics.UNKNOWN : loaded);
        return loaded;
    }

    @Nullable
    private TextureMetrics loadTextureMetricsFromResource(@Nonnull String texturePath) {
        String classpathLocation = "/Common/UI/Custom/" + texturePath;
        try (InputStream stream = RadialMenuPage.class.getResourceAsStream(classpathLocation)) {
            if (stream == null) {
                return null;
            }
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                return null;
            }
            if (image.getWidth() <= 0 || image.getHeight() <= 0) {
                return null;
            }
            int width = image.getWidth();
            int height = image.getHeight();
            int alphaLeft = width;
            int alphaTop = height;
            int alphaRight = 0;
            int alphaBottom = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int alpha = (image.getRGB(x, y) >>> 24) & 0xff;
                    if (alpha == 0) {
                        continue;
                    }
                    alphaLeft = Math.min(alphaLeft, x);
                    alphaTop = Math.min(alphaTop, y);
                    alphaRight = Math.max(alphaRight, x + 1);
                    alphaBottom = Math.max(alphaBottom, y + 1);
                }
            }
            if (alphaRight <= alphaLeft || alphaBottom <= alphaTop) {
                alphaLeft = 0;
                alphaTop = 0;
                alphaRight = width;
                alphaBottom = height;
            }
            return new TextureMetrics(
                    width,
                    height,
                    alphaLeft,
                    alphaTop,
                    alphaRight,
                    alphaBottom
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    private void applyLabelGeometry(@Nonnull UICommandBuilder commandBuilder,
                                    @Nonnull String labelSelector,
                                    @Nonnull RadialMenuConfig.Geometry geometry,
                                    int optionIndex,
                                    @Nonnull String texturePrefix,
                                    boolean vectorMode) {
        double scale = safePositive(geometry.getOuterDiameterPx(), RadialMenuConfig.Geometry.DEFAULT_OUTER_DIAMETER)
                / (double) BASE_OUTER_DIAMETER;
        Rect base = BASE_LABEL_RECTS[optionIndex];
        int width = Math.max(40, (int) Math.round(base.width * scale));
        int height = Math.max(20, (int) Math.round(base.height * scale));
        int labelRadius = safePositive(geometry.getLabelRadiusPx(), RadialMenuConfig.Geometry.DEFAULT_LABEL_RADIUS);
        double angleDegrees = -90 + optionIndex * 45.0;
        if (!vectorMode && !isLegacyTexturePrefix(texturePrefix)) {
            angleDegrees += TEXTURE_LABEL_ANGLE_OFFSET_DEGREES;
        }
        double radians = Math.toRadians(angleDegrees);
        int centerX = (int) Math.round(BASE_CENTER_X + Math.cos(radians) * labelRadius);
        int centerY = (int) Math.round(BASE_CENTER_Y + Math.sin(radians) * labelRadius);
        Rect computed = centeredRect(centerX, centerY, width, height);
        commandBuilder.setObject(labelSelector + ".Anchor", computed.toAnchorObject());
    }

    private int safePositive(int value, int fallback) {
        return value > 0 ? value : fallback;
    }

    @Nonnull
    private Rect centerTextRect(@Nonnull Rect centerRect) {
        int width = Math.max(140, centerRect.width - 40);
        int height = Math.max(90, centerRect.height - 40);
        return centeredRect(centerRect.centerX(), centerRect.centerY(), width, height);
    }

    @Nonnull
    private static Rect centeredRect(int centerX, int centerY, int width, int height) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        return new Rect(
                centerY - (safeHeight / 2),
                centerX - (safeWidth / 2),
                safeWidth,
                safeHeight
        );
    }

    @Nonnull
    private String resolveTexturePrefix(@Nonnull RadialMenuConfig menu) {
        if (menu.getVisual().getRenderMode() == RenderMode.Vector) {
            if (isVectorTexturePrefixComplete(VECTOR_TEXTURE_PREFIX)) {
                return VECTOR_TEXTURE_PREFIX;
            }
            if (logger != null) {
                logger.at(Level.WARNING).log(
                        "RadialMenu: Vector texture set '" + VECTOR_TEXTURE_PREFIX + "' is incomplete. Falling back to '"
                                + RadialMenuVisualResolver.LEGACY_TEXTURE_PREFIX + "'."
                );
            }
            return RadialMenuVisualResolver.LEGACY_TEXTURE_PREFIX;
        }
        return RadialMenuVisualResolver.resolveTexturePrefix(
                menu,
                this::isTexturePrefixComplete,
                warning -> {
                    if (logger != null) {
                        logger.at(Level.WARNING).log("RadialMenu: " + warning);
                    }
                }
        );
    }

    private boolean isVectorTexturePrefixComplete(@Nonnull String prefix) {
        if (!resourceExists(prefix + "/CommandWheelCenterPanel_Fill.png")) {
            return false;
        }
        if (!resourceExists(prefix + "/CommandWheelCenterPanel_Border.png")) {
            return false;
        }
        for (int i = 0; i < MAX_OPTIONS; i++) {
            if (!resourceExists(prefix + "/CommandWheelSlice" + i + "_Fill.png")) {
                return false;
            }
            if (!resourceExists(prefix + "/CommandWheelSlice" + i + "_Border.png")) {
                return false;
            }
        }
        return true;
    }

    private boolean isTexturePrefixComplete(@Nonnull String prefix) {
        return RadialMenuVisualResolver.textureSetLooksComplete(prefix, this::resourceExists);
    }

    private boolean resourceExists(@Nonnull String texturePath) {
        String normalized = texturePath.replace('\\', '/');
        String classpathLocation = "/Common/UI/Custom/" + normalized;
        try (InputStream stream = RadialMenuPage.class.getResourceAsStream(classpathLocation)) {
            return stream != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Nonnull
    private String resolveCurrentLabel() {
        if (selectedOptionId == null || selectedOptionId.isBlank()) {
            return RadialMenuLocalizedText.resolve(playerRef, "radialmenu.ui.current.none");
        }
        DisplayOption selected = findOption(selectedOptionId);
        if (selected == null) {
            return RadialMenuLocalizedText.format(
                    playerRef,
                    "radialmenu.ui.current.value",
                    selectedOptionId
            );
        }
        return RadialMenuLocalizedText.format(
                playerRef,
                "radialmenu.ui.current.value",
                selected.label()
        );
    }

    @Nullable
    private DisplayOption findOption(@Nullable String optionId) {
        if (optionId == null || optionId.isBlank()) {
            return null;
        }
        for (DisplayOption option : options) {
            if (option.id().equalsIgnoreCase(optionId.trim())) {
                return option;
            }
        }
        return null;
    }

    private int indexOfOption(@Nullable String optionId) {
        if (optionId == null || optionId.isBlank()) {
            return -1;
        }
        for (int i = 0; i < options.length; i++) {
            if (options[i].id().equalsIgnoreCase(optionId.trim())) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    private Option findOptionConfig(@Nullable String optionId) {
        if (optionId == null || optionId.isBlank()) {
            return null;
        }
        Option[] source = config.getOptions();
        if (source == null || source.length == 0) {
            return null;
        }
        for (Option option : source) {
            if (option == null || option.getId() == null) {
                continue;
            }
            if (option.getId().equalsIgnoreCase(optionId.trim())) {
                return option;
            }
        }
        return null;
    }

    @Nullable
    private Option firstOption() {
        Option[] source = config.getOptions();
        if (source == null || source.length == 0) {
            return null;
        }
        for (Option option : source) {
            if (option != null && option.getId() != null && !option.getId().isBlank()) {
                return option;
            }
        }
        return null;
    }

    @Nonnull
    private static DisplayOption[] buildOptions(@Nonnull PlayerRef playerRef, @Nonnull RadialMenuConfig config) {
        Option[] source = config.getOptions();
        if (source == null || source.length == 0) {
            return new DisplayOption[0];
        }
        List<DisplayOption> out = new ArrayList<>(MAX_OPTIONS);
        for (Option option : source) {
            if (option == null || option.getId() == null || option.getId().isBlank()) {
                continue;
            }
            out.add(new DisplayOption(option.getId(), resolveOptionLabel(playerRef, option)));
            if (out.size() >= MAX_OPTIONS) {
                break;
            }
        }
        return out.toArray(new DisplayOption[0]);
    }

    @Nonnull
    private static String resolveOptionLabel(@Nonnull PlayerRef playerRef, @Nonnull Option option) {
        if (option.getLabel() != null && !option.getLabel().isBlank()) {
            return option.getLabel();
        }
        if (option.getLabelKey() != null && !option.getLabelKey().isBlank()) {
            return RadialMenuLocalizedText.resolve(playerRef, option.getLabelKey());
        }
        if (option.getId() != null && !option.getId().isBlank()) {
            return option.getId();
        }
        return RadialMenuLocalizedText.resolve(playerRef, "radialmenu.ui.unknownOption");
    }

    @Nonnull
    private static PatchStyle buildPatchStyle(@Nonnull String texturePath, @Nullable String color) {
        PatchStyle style = new PatchStyle();
        style.setTexturePath(Value.of(texturePath.replace('\\', '/')));
        if (color != null && !color.isBlank()) {
            style.setColor(Value.of(color));
        }
        return style;
    }

    private record Rect(int top, int left, int width, int height) {
        @Nonnull
        private Rect scaleAround(int centerX, int centerY, double scale) {
            int newWidth = Math.max(1, (int) Math.round(width * scale));
            int newHeight = Math.max(1, (int) Math.round(height * scale));
            int currentCenterX = left + (width / 2);
            int currentCenterY = top + (height / 2);
            int shiftedCenterX = (int) Math.round(centerX + (currentCenterX - centerX) * scale);
            int shiftedCenterY = (int) Math.round(centerY + (currentCenterY - centerY) * scale);
            return centeredRect(shiftedCenterX, shiftedCenterY, newWidth, newHeight);
        }

        private int centerX() {
            return left + (width / 2);
        }

        private int centerY() {
            return top + (height / 2);
        }

        @Nonnull
        private Rect inset(int amount) {
            int safe = Math.max(0, amount);
            int newWidth = Math.max(1, width - safe * 2);
            int newHeight = Math.max(1, height - safe * 2);
            return centeredRect(centerX(), centerY(), newWidth, newHeight);
        }

        @Nonnull
        private Anchor toAnchorObject() {
            Anchor anchor = new Anchor();
            anchor.setTop(Value.of(top));
            anchor.setLeft(Value.of(left));
            anchor.setWidth(Value.of(width));
            anchor.setHeight(Value.of(height));
            return anchor;
        }
    }

    private record DisplayOption(String id, String label) {
    }

    private record Point(double x, double y) {
    }

    private record TextureMetrics(int width,
                                  int height,
                                  int alphaLeft,
                                  int alphaTop,
                                  int alphaRight,
                                  int alphaBottom) {
        private static final TextureMetrics UNKNOWN = new TextureMetrics(-1, -1, 0, 0, 0, 0);

        private boolean known() {
            return width > 0 && height > 0;
        }

        private boolean hasAlphaBounds() {
            return alphaRight > alphaLeft && alphaBottom > alphaTop;
        }
    }

    public static final class RadialMenuEventData {
        public static final BuilderCodec<RadialMenuEventData> CODEC = BuilderCodec.builder(
                        RadialMenuEventData.class,
                        RadialMenuEventData::new
                )
                .append(
                        new KeyedCodec<>(EVENT_OPTION_ID, Codec.STRING),
                        (data, value) -> data.optionId = value,
                        data -> data.optionId
                )
                .add()
                .append(
                        new KeyedCodec<>(EVENT_ACTION, Codec.STRING),
                        (data, value) -> data.action = value,
                        data -> data.action
                )
                .add()
                .build();

        private String optionId;
        private String action = ACTION_SELECT;
    }
}
