package com.alechilles.radialmenu.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alechilles.radialmenu.TestConfigFactory;
import com.alechilles.radialmenu.api.RadialMenuNpcActionContext;
import com.alechilles.radialmenu.api.RadialMenuNpcTarget;
import com.alechilles.radialmenu.config.RadialMenuConfig;
import com.alechilles.radialmenu.config.RadialMenuConfig.ExecutionMode;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.EmptyResourceStorage;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import sun.misc.Unsafe;

final class RadialMenuNpcOptionExecutionTest {
    private ComponentRegistry<EntityStore> componentRegistry;
    private Store<EntityStore> store;
    private RadialMenuNpcTarget target;
    private Player player;
    private RadialMenuCatalog catalog;

    @BeforeEach
    void setUp() {
        componentRegistry = new ComponentRegistry<>();
        var npcType = componentRegistry.registerComponent(NPCEntity.class, NPCEntity::new);
        store = componentRegistry.addStore(null, EmptyResourceStorage.get());
        var npcRef = store.addEntity(Archetype.of(npcType), AddReason.SPAWN);
        target = new RadialMenuNpcTarget(
                UUID.randomUUID(),
                npcRef,
                store.getComponent(npcRef, npcType),
                store
        );
        player = allocatePlayer();
        player.setLegacyUUID(UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        store.shutdown();
        componentRegistry.shutdown();
    }

    @Test
    void stateAndResultOptionsPassTheirPayloadsToNpcEffects() {
        RecordingNpcEffects effects = new RecordingNpcEffects();
        RadialMenuRuntimeService runtime = runtimeWith(effects, new RadialMenuNpcActionRegistry());
        RadialMenuConfig menu = TestConfigFactory.menu(
                "menus/npc",
                ExecutionMode.SelectAndArm,
                null,
                new String[0],
                TestConfigFactory.npcStateOption("follow", "Follow", "Following", "Moving"),
                TestConfigFactory.npcResultOption("stay", "Stay", "stay")
        );
        rebuild(menu);

        assertTrue(runtime.selectNpcOption(player, "menus/npc", "follow", target, "test"));
        assertEquals("Following", effects.state);
        assertEquals("Moving", effects.subState);
        assertSame(target, effects.target);

        assertTrue(runtime.selectNpcOption(player, "menus/npc", "stay", target, "test"));
        assertEquals(player.getUuid(), effects.playerUuid);
        assertEquals("menus/npc", effects.menuKey);
        assertEquals("stay", effects.resultId);
        assertTrue(effects.nowMillis > 0L);
    }

    @Test
    void registeredNpcActionReceivesTheResolvedTargetAndPayloadImmediately() {
        RadialMenuNpcActionRegistry npcActions = new RadialMenuNpcActionRegistry();
        AtomicReference<RadialMenuNpcActionContext> received = new AtomicReference<>();
        npcActions.register("Example.Inspect", context -> {
            received.set(context);
            return true;
        });
        RadialMenuRuntimeService runtime = runtimeWith(new RecordingNpcEffects(), npcActions);
        RadialMenuConfig menu = TestConfigFactory.menu(
                "menus/npc-action",
                ExecutionMode.SelectAndArm,
                null,
                new String[0],
                TestConfigFactory.npcActionOption(
                        "inspect",
                        "Inspect",
                        "Example.Inspect",
                        Map.of("detail", "short")
                )
        );
        rebuild(menu);

        assertTrue(runtime.selectNpcOption(player, "menus/npc-action", "inspect", target, "test"));

        RadialMenuNpcActionContext context = received.get();
        assertSame(player, context.player());
        assertSame(target, context.npcTarget());
        assertEquals("menus/npc-action", context.menuKey());
        assertEquals("inspect", context.optionId());
        assertEquals(Map.of("detail", "short"), context.payload());
    }

    @Test
    void npcOnlyOptionCannotExecuteWithoutATarget() {
        RecordingNpcEffects effects = new RecordingNpcEffects();
        RadialMenuRuntimeService runtime = runtimeWith(effects, new RadialMenuNpcActionRegistry());
        RadialMenuConfig menu = TestConfigFactory.menu(
                "menus/npc",
                ExecutionMode.SelectAndRun,
                null,
                new String[0],
                TestConfigFactory.npcStateOption("follow", "Follow", "Following", null)
        );
        rebuild(menu);

        assertFalse(runtime.selectOption(player, "menus/npc", "follow", null, "test"));
        assertEquals(0, effects.callCount);
    }

    private RadialMenuRuntimeService runtimeWith(RecordingNpcEffects effects,
                                                  RadialMenuNpcActionRegistry npcActions) {
        catalog = new RadialMenuCatalog();
        return new RadialMenuRuntimeService(
                catalog,
                new RadialMenuSessionStore(),
                new RadialMenuActionRegistry(),
                npcActions,
                new PlayerCommandDispatcher(null, null),
                (ignoredPlayer, ignoredOption, ignoredContext) -> false,
                effects,
                null
        );
    }

    private void rebuild(RadialMenuConfig menu) {
        catalog.rebuild(Map.of(menu.getId(), menu), null);
    }

    private static Player allocatePlayer() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Player) ((Unsafe) field.get(null)).allocateInstance(Player.class);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Could not create a constructor-free Player test fixture.", ex);
        }
    }

    private static final class RecordingNpcEffects implements RadialMenuNpcEffectExecutor {
        private int callCount;
        private RadialMenuNpcTarget target;
        private String state;
        private String subState;
        private UUID playerUuid;
        private String menuKey;
        private String resultId;
        private long nowMillis;

        @Override
        public boolean setState(RadialMenuNpcTarget target, String state, String subState) {
            callCount++;
            this.target = target;
            this.state = state;
            this.subState = subState;
            return true;
        }

        @Override
        public boolean emitResult(RadialMenuNpcTarget target,
                                  UUID playerUuid,
                                  String menuKey,
                                  String resultId,
                                  long nowMillis) {
            callCount++;
            this.target = target;
            this.playerUuid = playerUuid;
            this.menuKey = menuKey;
            this.resultId = resultId;
            this.nowMillis = nowMillis;
            return true;
        }
    }
}
