---
title: "Java API and Registered Actions"
order: 2
published: true
draft: false
---

# Java API and Registered Actions

Parent: [Developer Integration](/mod/alecs-radial-menu/developer-integration) · [Home](/mod/alecs-radial-menu/home)

## Get the API

Request the API after Alec's Radial Menu has completed setup:

```java
RadialMenuApi api = RadialMenuMod.getApiInstance();
if (api == null) {
    return;
}
```

Declare Alec's Radial Menu as a dependency in your mod if its API is required.

## API methods

| Method | Purpose |
| --- | --- |
| `openMenu(player, menuKey)` | Opens a menu without an NPC target. |
| `openNpcMenu(player, menuKey, npcRef, accessor)` | Opens a target-aware menu. |
| `executeSelected(player, menuKey)` | Runs the player's selected or default option. |
| `registerActionHandler(actionId, handler)` | Registers a player-only action. |
| `registerNpcActionHandler(actionId, handler)` | Registers a target-aware action. |
| `listActionIds()` | Lists player-only handler IDs. |
| `listNpcActionIds()` | Lists target-aware handler IDs. |
| `listMenuKeys()` | Lists active, valid menu keys. |

The open and execute methods return `false` if the request cannot run.

## Register a player action

```java
AutoCloseable registration = api.registerActionHandler("Example.Ping", context -> {
    Player player = context.player();
    String mode = context.payload().getOrDefault("mode", "normal");
    return runPing(player, mode);
});
```

Use this menu option:

```json
{
  "Type": "InvokeRegisteredAction",
  "Id": "ping",
  "Label": "Ping",
  "ActionId": "Example.Ping",
  "Payload": {
    "mode": "normal"
  }
}
```

The context contains the player, menu key, option ID, action ID, source, and an immutable string payload.

## Register an NPC action

```java
AutoCloseable npcRegistration = api.registerNpcActionHandler("Example.InspectNpc", context -> {
    RadialMenuNpcTarget target = context.npcTarget();
    return inspect(target.reference(), target.npc(), target.store(), context.payload());
});
```

The target is live only during the callback. Do not retain `RadialMenuNpcTarget`, its reference, NPC component, or store after the callback returns.

## Remove registrations

Both registration methods return an `AutoCloseable`. Close it when your mod shuts down or no longer owns the action ID:

```java
registration.close();
npcRegistration.close();
```

Return `true` only when the handler completes its work. A missing handler or a `false` result makes the option fail and prevents success feedback.
