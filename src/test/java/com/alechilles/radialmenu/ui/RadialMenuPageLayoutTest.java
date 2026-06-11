package com.alechilles.radialmenu.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class RadialMenuPageLayoutTest {
    @Test
    void textureLabelsUseSliceCenterAngle() {
        assertEquals(22.5, RadialMenuPage.TEXTURE_LABEL_ANGLE_OFFSET_DEGREES, 0.0001);
    }

    @Test
    void closeButtonSitsBelowWheel() throws IOException {
        String template = new String(
                RadialMenuPageLayoutTest.class.getResourceAsStream("/Common/UI/Custom/RadialMenu.ui").readAllBytes(),
                StandardCharsets.UTF_8
        );

        assertTrue(template.contains("TextButton #RadialMenuCloseButton"));
        assertTrue(template.contains("Anchor: (Bottom: -36, Width: 180, Height: 44, Left: 370);"));
        assertTrue(template.contains("Style: $C.@SecondaryTextButtonStyle;"));
    }
}
