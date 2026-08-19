package com.alechilles.radialmenu.ui;

import javax.annotation.Nonnull;

import com.alechilles.radialmenu.config.RadialMenuConfig.Option;

final class RadialMenuOptionAvailability {
    private RadialMenuOptionAvailability() {
    }

    static boolean isEnabled(@Nonnull Option option, boolean hasNpcTarget) {
        return !option.requiresNpcTarget() || hasNpcTarget;
    }
}
