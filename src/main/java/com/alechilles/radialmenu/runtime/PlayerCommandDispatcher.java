package com.alechilles.radialmenu.runtime;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import javax.annotation.Nullable;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;

/**
 * Executes a command as a player while staying compatible with API changes across snapshots.
 */
public final class PlayerCommandDispatcher {
    private static final List<String> REGISTRY_METHOD_NAMES = List.of(
            "executeCommand",
            "execute",
            "runCommand",
            "run",
            "dispatch"
    );

    private static final List<String> PLAYER_METHOD_NAMES = List.of(
            "executeCommand",
            "performCommand",
            "runCommand",
            "chat",
            "sendChatMessage"
    );

    @Nullable
    private final JavaPlugin plugin;
    @Nullable
    private final HytaleLogger logger;

    public PlayerCommandDispatcher(@Nullable JavaPlugin plugin, @Nullable HytaleLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public boolean dispatch(@Nullable Player player, @Nullable String commandText) {
        if (player == null || commandText == null || commandText.isBlank()) {
            return false;
        }
        String command = commandText.trim();
        if (dispatchThroughCommandManager(player, command)) {
            return true;
        }
        if (dispatchThroughCommandRegistry(player, command)) {
            return true;
        }
        return dispatchThroughPlayer(player, command);
    }

    private boolean dispatchThroughCommandManager(Player player, String command) {
        CommandManager commandManager;
        try {
            commandManager = CommandManager.get();
        } catch (Throwable ignored) {
            return false;
        }
        if (commandManager == null) {
            return false;
        }
        PlayerRef playerRef = player.getPlayerRef();
        if (playerRef == null) {
            return false;
        }
        String normalized = stripLeadingSlash(command);
        if (normalized == null || normalized.isBlank()) {
            return false;
        }
        try {
            CompletableFuture<Void> future = commandManager.handleCommand(playerRef, normalized);
            if (future != null) {
                future.whenComplete((ignored, throwable) -> {
                    if (throwable != null && logger != null) {
                        logger.at(Level.WARNING).withCause(throwable).log(
                                "RadialMenu command dispatch failed via CommandManager.handleCommand"
                        );
                    }
                });
            }
            return true;
        } catch (Throwable ex) {
            if (logger != null) {
                logger.at(Level.WARNING).withCause(ex).log(
                        "RadialMenu command dispatch threw via CommandManager.handleCommand"
                );
            }
            return false;
        }
    }

    private boolean dispatchThroughCommandRegistry(Player player, String command) {
        if (plugin == null) {
            return false;
        }
        Object commandRegistry;
        try {
            commandRegistry = plugin.getCommandRegistry();
        } catch (Throwable ignored) {
            return false;
        }
        if (commandRegistry == null) {
            return false;
        }
        for (String methodName : REGISTRY_METHOD_NAMES) {
            if (tryInvoke(commandRegistry, methodName, player, command)) {
                return true;
            }
            Object playerRef = player.getPlayerRef();
            if (playerRef != null && tryInvoke(commandRegistry, methodName, playerRef, command)) {
                return true;
            }
        }
        return false;
    }

    private boolean dispatchThroughPlayer(Player player, String command) {
        for (String methodName : PLAYER_METHOD_NAMES) {
            if (tryInvoke(player, methodName, command)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryInvoke(Object target, String methodName, Object... args) {
        Objects.requireNonNull(target, "target");
        Method method = findMethod(target.getClass(), methodName, args);
        if (method == null) {
            return false;
        }
        try {
            Object result = method.invoke(target, args);
            if (result == null) {
                return true;
            }
            if (result instanceof Boolean bool) {
                return bool;
            }
            return true;
        } catch (Throwable ex) {
            if (logger != null) {
                logger.at(Level.WARNING).withCause(ex).log(
                        "RadialMenu command dispatch failed via " + target.getClass().getSimpleName() + "." + methodName
                );
            }
            return false;
        }
    }

    @Nullable
    private Method findMethod(Class<?> type, String methodName, Object[] args) {
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(methodName)) {
                    continue;
                }
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length != args.length) {
                    continue;
                }
                if (!isCompatible(parameters, args)) {
                    continue;
                }
                method.setAccessible(true);
                return method;
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private boolean isCompatible(Class<?>[] parameterTypes, Object[] args) {
        for (int i = 0; i < parameterTypes.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                if (parameterTypes[i].isPrimitive()) {
                    return false;
                }
                continue;
            }
            if (!parameterTypes[i].isAssignableFrom(arg.getClass())) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private static String stripLeadingSlash(@Nullable String command) {
        if (command == null) {
            return null;
        }
        String value = command.trim();
        if (value.startsWith("/")) {
            value = value.substring(1).trim();
        }
        return value;
    }
}
