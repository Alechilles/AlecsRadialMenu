package com.alechilles.radialmenu.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.alechilles.radialmenu.TestConfigFactory;
import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecutionMode;
import com.alechilles.radialmenu.config.RadialMenuConfig.Option;
import com.alechilles.radialmenu.config.RadialMenuConfig.RenderMode;

class RadialMenuVisualResolverTest {
    @Test
    void optionOverrideKeepsFontButUsesBuiltInColors() {
        Option option = TestConfigFactory.commandOption("config", "Config", "/tw config");
        TestConfigFactory.setOptionVisualOverride(
                option,
                TestConfigFactory.optionVisualOverride(
                        22,
                        TestConfigFactory.statePalette(
                                TestConfigFactory.stateColors("#112233", "#445566", "#778899"),
                                null,
                                null,
                                TestConfigFactory.stateColors("#224466", "#ffffff", "#113355"),
                                null
                        )
                )
        );

        RadialMenuConfig config = TestConfigFactory.menu(
                "menus/example/override",
                ExecutionMode.SelectAndArm,
                "config",
                new String[0],
                option
        );

        RadialMenuVisualResolver.ResolvedOptionVisual resolved = RadialMenuVisualResolver.resolveOptionVisual(config, option, false);
        assertEquals(22, resolved.labelFontSize());
        assertEquals("#3b5263", resolved.defaultState().fillColor());
        assertEquals("#d6e0ec", resolved.defaultState().textColor());
        assertEquals("#1b2730", resolved.defaultState().borderColor());

        RadialMenuVisualResolver.ResolvedOptionVisual selected = RadialMenuVisualResolver.resolveOptionVisual(config, option, true);
        assertEquals("#5d829f", selected.defaultState().fillColor());
        assertEquals("#ffffff", selected.defaultState().textColor());
        assertEquals("#30495b", selected.defaultState().borderColor());
    }

    @Test
    void texturePrefixFallsBackWhenCustomSetIncomplete() {
        RadialMenuConfig config = TestConfigFactory.menu(
                "menus/example/texture",
                ExecutionMode.SelectAndArm,
                null,
                new String[0],
                TestConfigFactory.commandOption("config", "Config", "/tw config")
        );

        TestConfigFactory.setVisual(
                config,
                TestConfigFactory.visual(
                        RenderMode.Texture,
                        640,
                        300,
                        234,
                        300,
                        2,
                        15,
                        config.getVisual().getStates(),
                        "Custom/Prefix"
                )
        );

        AtomicBoolean warned = new AtomicBoolean(false);
        String prefix = RadialMenuVisualResolver.resolveTexturePrefix(
                config,
                ignored -> false,
                ignored -> warned.set(true)
        );

        assertTrue(warned.get());
        assertEquals(RadialMenuVisualResolver.DEFAULT_TEXTURE_PREFIX, prefix);
    }

    @Test
    void texturePrefixDefaultsToBuiltInTextureSet() {
        RadialMenuConfig config = TestConfigFactory.menu(
                "menus/example/default-texture",
                ExecutionMode.SelectAndArm,
                null,
                new String[0],
                TestConfigFactory.commandOption("config", "Config", "/tw config")
        );

        String prefix = RadialMenuVisualResolver.resolveTexturePrefix(
                config,
                ignored -> true,
                ignored -> {
                }
        );

        assertEquals(RadialMenuVisualResolver.DEFAULT_TEXTURE_PREFIX, prefix);
    }

    @Test
    void explicitBuiltInTexturePrefixSkipsCustomCompletenessCheck() {
        RadialMenuConfig config = TestConfigFactory.menu(
                "menus/example/explicit-default-texture",
                ExecutionMode.SelectAndArm,
                null,
                new String[0],
                TestConfigFactory.commandOption("config", "Config", "/tw config")
        );
        TestConfigFactory.setVisual(
                config,
                TestConfigFactory.visual(
                        RenderMode.Texture,
                        640,
                        300,
                        234,
                        300,
                        2,
                        15,
                        config.getVisual().getStates(),
                        RadialMenuVisualResolver.DEFAULT_TEXTURE_PREFIX
                )
        );
        AtomicBoolean checked = new AtomicBoolean(false);
        AtomicBoolean warned = new AtomicBoolean(false);

        String prefix = RadialMenuVisualResolver.resolveTexturePrefix(
                config,
                ignored -> {
                    checked.set(true);
                    return false;
                },
                ignored -> warned.set(true)
        );

        assertFalse(checked.get());
        assertFalse(warned.get());
        assertEquals(RadialMenuVisualResolver.DEFAULT_TEXTURE_PREFIX, prefix);
    }

    @Test
    void builtInTextureReusesDefaultSliceWithRuntimeStateColors() {
        RadialMenuVisualResolver.ResolvedButtonTextures textures =
                RadialMenuVisualResolver.resolveButtonTextures(
                        RadialMenuVisualResolver.DEFAULT_TEXTURE_PREFIX,
                        false,
                        3
                );

        assertEquals("RadialMenu/Default/CommandWheelSlice3_Default.png", textures.defaultTexture());
        assertEquals(textures.defaultTexture(), textures.hoverTexture());
        assertEquals(textures.defaultTexture(), textures.pressedTexture());
        assertEquals("#b9dcff", textures.hoverColor());
        assertNull(textures.pressedColor());
    }

    @Test
    void textureSetCompletenessChecksExpectedFileSet() {
        assertFalse(
                RadialMenuVisualResolver.textureSetLooksComplete("RadialMenu", path -> false)
        );
        assertTrue(
                RadialMenuVisualResolver.textureSetLooksComplete("RadialMenu", path -> true)
        );
    }
}
