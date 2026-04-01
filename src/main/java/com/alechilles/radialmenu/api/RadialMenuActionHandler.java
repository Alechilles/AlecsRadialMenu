package com.alechilles.radialmenu.api;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface RadialMenuActionHandler {
    boolean handle(@Nonnull RadialMenuActionContext context);
}
