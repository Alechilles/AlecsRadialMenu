package com.alechilles.radialmenu.api;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface RadialMenuNpcActionHandler {
    /** Handles one selection. The context's NPC target is valid only during this call. */
    boolean handle(@Nonnull RadialMenuNpcActionContext context);
}
