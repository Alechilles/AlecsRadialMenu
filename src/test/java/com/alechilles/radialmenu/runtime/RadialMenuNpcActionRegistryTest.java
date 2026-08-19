package com.alechilles.radialmenu.runtime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

final class RadialMenuNpcActionRegistryTest {
    @Test
    void registrationIsCaseInsensitiveAndRejectsDuplicates() {
        RadialMenuNpcActionRegistry registry = new RadialMenuNpcActionRegistry();

        registry.register("Example.Inspect", context -> true);

        assertNotNull(registry.get("example.inspect"));
        assertNotNull(registry.get("EXAMPLE.INSPECT"));
        assertThrows(
                IllegalStateException.class,
                () -> registry.register("example.inspect", context -> false)
        );
    }

    @Test
    void closeRemovesOnlyItsOwnRegistration() throws Exception {
        RadialMenuNpcActionRegistry registry = new RadialMenuNpcActionRegistry();
        AutoCloseable firstRegistration = registry.register("Example.Inspect", context -> true);

        firstRegistration.close();
        AutoCloseable secondRegistration = registry.register("Example.Inspect", context -> false);
        firstRegistration.close();

        assertNotNull(registry.get("example.inspect"));
        secondRegistration.close();
        assertNull(registry.get("example.inspect"));
    }

    @Test
    void listedActionIdsAreImmutable() {
        RadialMenuNpcActionRegistry registry = new RadialMenuNpcActionRegistry();
        registry.register("Example.Inspect", context -> true);

        Set<String> actionIds = registry.listActionIds();

        assertTrue(actionIds.contains("example.inspect"));
        assertThrows(UnsupportedOperationException.class, () -> actionIds.add("other"));
    }
}
