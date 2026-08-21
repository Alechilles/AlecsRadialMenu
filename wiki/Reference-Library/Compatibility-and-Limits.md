---
title: "Compatibility and Limits"
order: 2
published: true
draft: false
---

# Compatibility and Limits

Parent: [Reference Library](/mod/alecs-radial-menu/reference-library) · [Home](/mod/alecs-radial-menu/home)

## Declared compatibility

Alec's Radial Menu version `2.0.1` declares:

```text
>=0.5.0 <0.7.0
```

The plugin also requires the Hytale asset and NPC modules.

## Runtime limits

| Area | Limit |
| --- | --- |
| Menu size | One to eight options. |
| Default selection | `DefaultOptionId`, then the first valid option. |
| Player selection | Stored per player and menu until disconnect. |
| NPC menu mode | Always `SelectAndRun`. |
| NPC target | Resolved at selection time and valid only for the action call. |
| Named NPC result | Expires after five seconds and is consumed once. |
| Standalone interaction | `InteractionType` defaults to `Primary`; `Equipped` is unsupported. |
| Success feedback | Chat and HUD text work. Sound and particle fields are reserved. |
| Texture fallback | An incomplete custom set falls back to `RadialMenu/Default`. |

## Asset validation

The runtime skips a menu when it has no options, more than eight options, duplicate or blank IDs, missing required option fields, unsupported option types, or invalid visual values.

Menu and option ID lookup is case-insensitive. Use one consistent spelling so logs and assets remain easy to compare.

## NPC compatibility notes

`OpenRadialMenu` and `RadialMenuResult` are valid only in NPC interaction instructions. Direct state changes validate the state and substate names before they run.

For a state sensor that has no native state setter in the role, use `IgnoreMissingSetState: true`. See [States, Results, and NPC Actions](/mod/alecs-radial-menu/states-results-and-npc-actions).
