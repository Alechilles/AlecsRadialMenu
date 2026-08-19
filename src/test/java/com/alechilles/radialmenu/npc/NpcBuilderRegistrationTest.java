package com.alechilles.radialmenu.npc;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class NpcBuilderRegistrationTest {

    @Test
    void customNpcBuildersCanBeCreatedBeforeAnInstructionContextExists() {
        assertDoesNotThrow(BuilderActionOpenRadialMenu::new);
        assertDoesNotThrow(BuilderSensorRadialMenuResult::new);
    }
}
