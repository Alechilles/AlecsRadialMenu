package com.alechilles.radialmenu.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.alechilles.radialmenu.TestConfigFactory;
import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecutionMode;
import com.alechilles.radialmenu.config.RadialMenuConfig.Option;
import com.alechilles.radialmenu.config.RadialMenuConfig.RenderMode;
import com.hypixel.hytale.protocol.InteractionType;

class RadialMenuCatalogValidationTest {
    @Test
    void bundledExampleUsesDefaultTextureVisualsWithoutRedundantOverrides() throws IOException {
        String example = new String(
                RadialMenuCatalogValidationTest.class
                        .getResourceAsStream("/Server/RadialMenu/Menus/Example_Basic.json")
                        .readAllBytes()
        );

        assertFalse(example.contains("\"Visual\""));
        assertFalse(example.contains("\"VisualOverride\""));
        assertFalse(example.contains("\"FillColor\""));
        assertFalse(example.contains("\"TextureSet\""));
        assertFalse(example.contains("\"RenderMode\""));
        assertFalse(example.contains("\"Test "));
        assertFalse(example.contains("/tw config"));
        assertTrue(example.contains("\"Type\": \"RunInteraction\""));
        assertTrue(example.contains("\"RootInteraction\": \"Root_Unarmed_Swing_Left\""));
    }

    @Test
    void validateAcceptsWellFormedMenu() {
        RadialMenuCatalog catalog = new RadialMenuCatalog();
        RadialMenuConfig config = TestConfigFactory.menu(
                "example/basic",
                ExecutionMode.SelectAndArm,
                "config",
                new String[] {"Alec_Radial_Menu_Example"},
                TestConfigFactory.commandOption("config", "/tw config", "/tw config"),
                TestConfigFactory.actionOption("ping", "Ping", "Example.Ping", java.util.Map.of("k", "v")),
                TestConfigFactory.interactionOption(
                        "swing",
                        "Swing",
                        "Root_Unarmed_Swing_Left",
                        InteractionType.Primary
                )
        );

        List<String> issues = catalog.validate(config);
        assertTrue(issues.isEmpty(), "Expected no validation issues but got: " + issues);
    }

    @Test
    void validateRejectsTooManyDuplicateAndMismatchedDefaults() {
        RadialMenuCatalog catalog = new RadialMenuCatalog();
        Option[] options = new Option[] {
                TestConfigFactory.commandOption("one", "One", "/one"),
                TestConfigFactory.commandOption("two", "Two", "/two"),
                TestConfigFactory.commandOption("three", "Three", "/three"),
                TestConfigFactory.commandOption("four", "Four", "/four"),
                TestConfigFactory.commandOption("five", "Five", "/five"),
                TestConfigFactory.commandOption("six", "Six", "/six"),
                TestConfigFactory.commandOption("seven", "Seven", "/seven"),
                TestConfigFactory.commandOption("eight", "Eight", "/eight"),
                TestConfigFactory.commandOption("eight", "Duplicate", "/dup")
        };
        RadialMenuConfig config = TestConfigFactory.menu(
                "example/invalid",
                ExecutionMode.SelectAndArm,
                "missing",
                new String[0],
                options
        );

        List<String> issues = catalog.validate(config);
        assertFalse(issues.isEmpty());
        assertTrue(issues.stream().anyMatch(x -> x.contains("hard cap")));
        assertTrue(issues.stream().anyMatch(x -> x.contains("Duplicate option Id")));
        assertTrue(issues.stream().anyMatch(x -> x.contains("DefaultOptionId")));
    }

    @Test
    void validateRejectsBlankActionPayloads() {
        RadialMenuCatalog catalog = new RadialMenuCatalog();
        RadialMenuConfig config = TestConfigFactory.menu(
                "example/action-invalid",
                ExecutionMode.SelectAndRun,
                null,
                new String[0],
                TestConfigFactory.commandOption("badCommand", "Bad", "  "),
                TestConfigFactory.actionOption("badAction", "Bad", " ", java.util.Map.of())
        );

        List<String> issues = catalog.validate(config);
        assertTrue(issues.stream().anyMatch(x -> x.contains("blank Command")));
        assertTrue(issues.stream().anyMatch(x -> x.contains("blank ActionId")));
    }

    @Test
    void validateRejectsBlankInteractionAndEquippedType() {
        RadialMenuCatalog catalog = new RadialMenuCatalog();
        RadialMenuConfig config = TestConfigFactory.menu(
                "example/interaction-invalid",
                ExecutionMode.SelectAndRun,
                null,
                new String[0],
                TestConfigFactory.interactionOption("blank", "Blank", " ", InteractionType.Primary),
                TestConfigFactory.interactionOption(
                        "equipped",
                        "Equipped",
                        "Root_Unarmed_Swing_Left",
                        InteractionType.Equipped
                )
        );

        List<String> issues = catalog.validate(config);
        assertTrue(issues.stream().anyMatch(x -> x.contains("blank RootInteraction")));
        assertTrue(issues.stream().anyMatch(x -> x.contains("cannot use InteractionType Equipped")));
    }

    @Test
    void validateRejectsInvalidVisualGeometryAndColors() {
        RadialMenuCatalog catalog = new RadialMenuCatalog();
        RadialMenuConfig config = TestConfigFactory.menu(
                "example/visual-invalid",
                ExecutionMode.SelectAndArm,
                null,
                new String[0],
                TestConfigFactory.commandOption("config", "Config", "/tw config")
        );

        TestConfigFactory.setVisual(
                config,
                TestConfigFactory.visual(
                        RenderMode.Vector,
                        100,
                        120,
                        10,
                        140,
                        -1,
                        0,
                        TestConfigFactory.statePalette(
                                TestConfigFactory.stateColors("not-a-color", "#ffffff", "#000000"),
                                TestConfigFactory.stateColors("#111111", "#ffffff", "#000000"),
                                TestConfigFactory.stateColors("#111111", "#ffffff", "#000000"),
                                TestConfigFactory.stateColors("#111111", "#ffffff", "#000000"),
                                TestConfigFactory.stateColors("#111111", "#ffffff", "#000000")
                        ),
                        null
                )
        );

        List<String> issues = catalog.validate(config);
        assertTrue(issues.stream().anyMatch(x -> x.contains("InnerDiameterPx must be smaller")));
        assertTrue(issues.stream().anyMatch(x -> x.contains("CenterDiameterPx must be <= InnerDiameterPx")));
        assertTrue(issues.stream().anyMatch(x -> x.contains("LabelRadiusPx must be between")));
        assertTrue(issues.stream().anyMatch(x -> x.contains("BorderThicknessPx must be >= 0")));
        assertTrue(issues.stream().anyMatch(x -> x.contains("Visual.Label.FontSize must be > 0")));
        assertTrue(issues.stream().anyMatch(x -> x.contains("FillColor is not a valid hex color string")));
    }

    @Test
    void validateRejectsInvalidOptionVisualOverride() {
        RadialMenuCatalog catalog = new RadialMenuCatalog();
        Option option = TestConfigFactory.commandOption("config", "Config", "/tw config");
        TestConfigFactory.setOptionVisualOverride(
                option,
                TestConfigFactory.optionVisualOverride(
                        0,
                        TestConfigFactory.statePalette(
                                TestConfigFactory.stateColors("#ffffff", "#ffffff", "#ffffff"),
                                TestConfigFactory.stateColors("#ffffff", "#ffffff", "#ffffff"),
                                TestConfigFactory.stateColors("#ffffff", "bad-color", "#ffffff"),
                                null,
                                null
                        )
                )
        );

        RadialMenuConfig config = TestConfigFactory.menu(
                "example/option-visual-invalid",
                ExecutionMode.SelectAndArm,
                null,
                new String[0],
                option
        );

        List<String> issues = catalog.validate(config);
        assertTrue(issues.stream().anyMatch(x -> x.contains("VisualOverride.LabelFontSize must be > 0")));
        assertTrue(issues.stream().anyMatch(x -> x.contains("TextColor is not a valid hex color string")));
    }
}
