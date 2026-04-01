package com.alechilles.radialmenu.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.alechilles.radialmenu.TestConfigFactory;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecutionMode;
import com.alechilles.radialmenu.config.RadialMenuConfig.Option;

class RadialMenuConfigTest {
    @Test
    void executionModeParsingDefaultsToSelectAndArm() {
        assertEquals(ExecutionMode.SelectAndArm, ExecutionMode.fromString(null));
        assertEquals(ExecutionMode.SelectAndArm, ExecutionMode.fromString(""));
        assertEquals(ExecutionMode.SelectAndArm, ExecutionMode.fromString("unknown"));
        assertEquals(ExecutionMode.SelectAndRun, ExecutionMode.fromString("SelectAndRun"));
    }

    @Test
    void idIsAssetKeyAndDefaultOptionResolves() {
        RadialMenuConfig config = TestConfigFactory.menu(
                "menus/example/basic",
                ExecutionMode.SelectAndArm,
                "cfg",
                new String[0],
                TestConfigFactory.commandOption("cfg", "Config", "/tw config"),
                TestConfigFactory.commandOption("other", "Other", "/say hi")
        );

        assertEquals("menus/example/basic", config.getId());
        Option option = config.resolveDefaultOption();
        assertNotNull(option);
        assertEquals("cfg", option.getId());
    }

    @Test
    void defaultOptionFallsBackToFirstWhenExplicitMissing() {
        RadialMenuConfig config = TestConfigFactory.menu(
                "menus/example/fallback",
                ExecutionMode.SelectAndArm,
                "missing",
                new String[0],
                TestConfigFactory.commandOption("first", "First", "/first")
        );

        Option option = config.resolveDefaultOption();
        assertNotNull(option);
        assertEquals("first", option.getId());

        RadialMenuConfig empty = TestConfigFactory.menu(
                "menus/example/empty",
                ExecutionMode.SelectAndArm,
                null,
                new String[0]
        );
        assertNull(empty.resolveDefaultOption());
    }
}
