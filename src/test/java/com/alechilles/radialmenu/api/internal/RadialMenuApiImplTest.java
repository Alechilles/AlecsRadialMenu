package com.alechilles.radialmenu.api.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.alechilles.radialmenu.TestConfigFactory;
import com.alechilles.radialmenu.api.RadialMenuApi;
import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecutionMode;
import com.alechilles.radialmenu.runtime.PlayerCommandDispatcher;
import com.alechilles.radialmenu.runtime.RadialMenuActionRegistry;
import com.alechilles.radialmenu.runtime.RadialMenuCatalog;
import com.alechilles.radialmenu.runtime.RadialMenuRuntimeService;
import com.alechilles.radialmenu.runtime.RadialMenuSessionStore;

class RadialMenuApiImplTest {
    @Test
    void registerActionHandlerAddsAndRemovesActionId() throws Exception {
        RadialMenuActionRegistry actions = new RadialMenuActionRegistry();
        RadialMenuCatalog catalog = new RadialMenuCatalog();
        RadialMenuRuntimeService runtime = new RadialMenuRuntimeService(
                catalog,
                new RadialMenuSessionStore(),
                actions,
                new PlayerCommandDispatcher(null, null),
                null
        );
        RadialMenuApi api = new RadialMenuApiImpl(actions, catalog, runtime);

        AutoCloseable closeable = api.registerActionHandler("Example.Action", context -> true);
        assertTrue(api.listActionIds().contains("example.action"));

        closeable.close();
        assertFalse(api.listActionIds().contains("example.action"));
    }

    @Test
    void listMenuKeysReflectsCatalog() {
        RadialMenuCatalog catalog = new RadialMenuCatalog();
        RadialMenuActionRegistry actions = new RadialMenuActionRegistry();
        RadialMenuRuntimeService runtime = new RadialMenuRuntimeService(
                catalog,
                new RadialMenuSessionStore(),
                actions,
                new PlayerCommandDispatcher(null, null),
                null
        );
        RadialMenuApi api = new RadialMenuApiImpl(actions, catalog, runtime);

        RadialMenuConfig menu = TestConfigFactory.menu(
                "example/api",
                ExecutionMode.SelectAndArm,
                "config",
                new String[] {"item.api"},
                TestConfigFactory.commandOption("config", "Config", "/tw config")
        );
        catalog.rebuild(Map.of("example/api", menu), null);

        assertTrue(api.listMenuKeys().contains("example/api"));
    }

    @Test
    void openAndExecuteCallsFailSafelyWithNullPlayer() {
        RadialMenuCatalog catalog = new RadialMenuCatalog();
        RadialMenuActionRegistry actions = new RadialMenuActionRegistry();
        RadialMenuRuntimeService runtime = new RadialMenuRuntimeService(
                catalog,
                new RadialMenuSessionStore(),
                actions,
                new PlayerCommandDispatcher(null, null),
                null
        );
        RadialMenuApi api = new RadialMenuApiImpl(actions, catalog, runtime);

        assertFalse(api.openMenu(null, "example/api"));
        assertFalse(api.executeSelected(null, "example/api"));
    }
}
