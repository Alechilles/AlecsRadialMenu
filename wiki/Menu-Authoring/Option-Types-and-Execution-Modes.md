---
title: "Option Types and Execution Modes"
order: 3
published: true
draft: false
---

# Option Types and Execution Modes

Parent: [Menu Authoring](/mod/alecs-radial-menu/menu-authoring) · [Home](/mod/alecs-radial-menu/home)

## Option types

| Type | Extra fields | Behavior |
| --- | --- | --- |
| `ExecuteCommand` | `Command` | Runs command text as the player. |
| `InvokeRegisteredAction` | `ActionId`, optional `Payload` | Calls a Java action handler. |
| `RunInteraction` | `RootInteraction`, optional `InteractionType` | Starts a native Hytale root interaction. |
| `SetNpcState` | `State`, optional `SubState` | Sets the live target NPC state. |
| `EmitNpcResult` | `ResultId` | Sends a short-lived result to the target NPC role. |
| `InvokeRegisteredNpcAction` | `ActionId`, optional `Payload` | Calls a Java handler with the live NPC target. |

`Payload` is a string-to-string map.

```json
{
  "Type": "InvokeRegisteredAction",
  "Id": "inspect",
  "Label": "Inspect",
  "ActionId": "Example.Inspect",
  "Payload": {
    "detail": "full"
  }
}
```

## `SelectAndArm`

This is the default mode. Selecting a slice stores it for that player and menu. A later `ExecuteSelected` item interaction runs the selected option.

For a `RunInteraction` option, `ExecuteSelected` enters the root interaction through the active item interaction context. The active binding supplies the interaction type.

Selections remain separate for each player and menu. The runtime clears them when the player disconnects.

## `SelectAndRun`

Selecting a slice runs it at once and also remembers it. A standalone `RunInteraction` uses its configured `InteractionType`, which defaults to `Primary`.

`Equipped` is not supported because standalone execution does not have an equipment slot.

## NPC target rules

An NPC-opened menu always uses `SelectAndRun`. This rule overrides the mode in the menu asset.

`SetNpcState`, `EmitNpcResult`, and `InvokeRegisteredNpcAction` need a live NPC target. They remain visible but disabled when the target is absent or no longer valid.

See [States, Results, and NPC Actions](/mod/alecs-radial-menu/states-results-and-npc-actions) for the correct NPC pattern.
