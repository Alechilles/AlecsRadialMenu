package com.alechilles.radialmenu.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.alechilles.radialmenu.TestConfigFactory;
import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecutionMode;

class RadialMenuRuntimeServiceTest {
    @Test
    void resolveMenuKeyUsesOverrideThenItemBinding() {
        RadialMenuCatalog catalog = new RadialMenuCatalog();
        RadialMenuSessionStore sessions = new RadialMenuSessionStore();
        RadialMenuActionRegistry actions = new RadialMenuActionRegistry();
        RadialMenuRuntimeService runtime = new RadialMenuRuntimeService(
                catalog,
                sessions,
                actions,
                new PlayerCommandDispatcher(null, null),
                null
        );

        RadialMenuConfig menu = TestConfigFactory.menu(
                "menus/example",
                ExecutionMode.SelectAndArm,
                null,
                new String[] {"Item.Example"},
                TestConfigFactory.commandOption("one", "One", "/say one")
        );
        catalog.rebuild(Map.of("menus/example", menu), null);

        assertEquals("menus/example", runtime.resolveMenuKey("menus/example", "other.item"));
        assertEquals("menus/example", runtime.resolveMenuKey(null, "item.example"));
        assertNull(runtime.resolveMenuKey(null, "unknown.item"));
    }

    @Test
    void runtimeCallsFailSafelyWithNullPlayer() {
        RadialMenuCatalog catalog = new RadialMenuCatalog();
        RadialMenuSessionStore sessions = new RadialMenuSessionStore();
        RadialMenuActionRegistry actions = new RadialMenuActionRegistry();
        RadialMenuRuntimeService runtime = new RadialMenuRuntimeService(
                catalog,
                sessions,
                actions,
                new PlayerCommandDispatcher(null, null),
                null
        );

        assertFalse(runtime.openMenu(null, "menus/example", null, "test"));
        assertFalse(runtime.selectOption(null, "menus/example", "one", null, "test"));
        assertFalse(runtime.executeSelected(null, "menus/example", null, "test"));
    }
}
