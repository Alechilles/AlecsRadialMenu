package com.alechilles.radialmenu.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.alechilles.radialmenu.TestConfigFactory;
import com.alechilles.radialmenu.config.RadialMenuConfig.Option;

final class RadialMenuPageNpcAvailabilityTest {
    @Test
    void npcOnlyOptionsStayVisibleButAreDisabledWithoutATarget() {
        Option npcOption = TestConfigFactory.npcStateOption("follow", "Follow", "Following", null);
        Option playerOption = TestConfigFactory.commandOption("wave", "Wave", "/wave");

        assertFalse(RadialMenuOptionAvailability.isEnabled(npcOption, false));
        assertTrue(RadialMenuOptionAvailability.isEnabled(npcOption, true));
        assertTrue(RadialMenuOptionAvailability.isEnabled(playerOption, false));
    }
}
