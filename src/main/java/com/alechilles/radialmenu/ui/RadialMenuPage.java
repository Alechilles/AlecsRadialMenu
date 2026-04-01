package com.alechilles.radialmenu.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.config.RadialMenuConfig.Option;
import com.alechilles.radialmenu.localization.RadialMenuLocalizedText;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class RadialMenuPage extends InteractiveCustomUIPage<RadialMenuPage.RadialMenuEventData> {
    public static final String UI_PATH = "RadialMenu.ui";

    private static final String EVENT_OPTION_ID = "OptionId";
    private static final String CLOSE_OPTION_ID = "__close__";
    private static final int MAX_OPTIONS = 8;

    private final String menuKey;
    private final DisplayOption[] options;
    private final String selectedOptionId;
    private final Consumer<String> selectionCallback;
    private boolean handled;

    public RadialMenuPage(@Nonnull PlayerRef playerRef,
                          @Nonnull String menuKey,
                          @Nonnull RadialMenuConfig config,
                          @Nullable String selectedOptionId,
                          @Nonnull Consumer<String> selectionCallback) {
        super(playerRef, CustomPageLifetime.CanDismiss, RadialMenuEventData.CODEC);
        this.menuKey = menuKey;
        this.options = buildOptions(playerRef, config);
        this.selectedOptionId = selectedOptionId;
        this.selectionCallback = selectionCallback;
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

        for (int i = 0; i < MAX_OPTIONS; i++) {
            String buttonSelector = "#CommandButton" + i;
            String labelSelector = "#CommandLabel" + i;
            if (i >= options.length) {
                commandBuilder.set(buttonSelector + ".Visible", false);
                commandBuilder.set(labelSelector + ".Visible", false);
                continue;
            }
            DisplayOption option = options[i];
            commandBuilder.set(buttonSelector + ".Visible", true);
            commandBuilder.set(buttonSelector + ".Text", "");
            commandBuilder.set(labelSelector + ".Visible", true);
            commandBuilder.set(labelSelector + ".Text", option.label());

            eventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    buttonSelector,
                    EventData.of(EVENT_OPTION_ID, option.id()),
                    false
            );
        }

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#RadialMenuCloseButton",
                EventData.of(EVENT_OPTION_ID, CLOSE_OPTION_ID),
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

    private record DisplayOption(String id, String label) {
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
                .build();

        private String optionId;
    }
}
