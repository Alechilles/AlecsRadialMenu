package com.alechilles.radialmenu.architecture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class InteractionContextLifetimeGuardTest {
    private static final Path RADIAL_MENU_INTERACTION = Paths.get(
            "src",
            "main",
            "java",
            "com",
            "alechilles",
            "radialmenu",
            "interactions",
            "RadialMenuInteraction.java"
    );

    @Test
    void activeInteractionContextDoesNotEscapeItsTick() throws IOException {
        String source = Files.readString(RADIAL_MENU_INTERACTION, StandardCharsets.UTF_8);

        assertFalse(
                source.contains("commandBuffer.run("),
                "CommandBuffer.run is deferred; the active InteractionContext is only valid during its current tick"
        );
        assertTrue(
                source.matches(
                        "(?s).*runtimeService\\.executeSelected\\(\\s*player,\\s*resolvedMenuKey,"
                                + "\\s*modeOverride,\\s*\"interaction\",\\s*context\\s*\\);.*"
                ),
                "ExecuteSelected must receive the active context synchronously so nested root interactions can run"
        );
    }
}
