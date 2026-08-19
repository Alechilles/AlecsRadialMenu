---
title: "States, Results, and NPC Actions"
order: 3
published: true
draft: false
---

# States, Results, and NPC Actions

Parent: [NPC Integration](/mod/alecs-radial-menu/npc-integration) · [Home](/mod/alecs-radial-menu/home)

Choose one of three target-aware paths.

## Set a state directly

Use `SetNpcState` for a simple state change:

```json
{
  "Type": "SetNpcState",
  "Id": "sleep",
  "Label": "Sleep",
  "State": "Sleep"
}
```

`SubState` is optional. The state and substate must exist in the role. An unknown name does not change the NPC.

When a role has a state sensor that is changed only through this external action, set `IgnoreMissingSetState` on that sensor:

```json
{
  "Type": "State",
  "State": "Sleep",
  "IgnoreMissingSetState": true
}
```

## Send a named result to the role

Use `EmitNpcResult` when the NPC instruction graph must run native actions:

```json
{
  "Type": "EmitNpcResult",
  "Id": "wake_and_mark",
  "Label": "Wake and Mark",
  "ResultId": "wake_and_mark"
}
```

Match the result in `InteractionInstruction`:

```json
{
  "Sensor": {
    "Type": "RadialMenuResult",
    "MenuId": "Example_Npc",
    "ResultId": "wake_and_mark"
  },
  "Actions": [
    {
      "Type": "SetFlag",
      "Name": "RadialMenuExampleMarked",
      "SetTo": true
    },
    {
      "Type": "State",
      "State": "Idle"
    }
  ]
}
```

The result expires after five seconds. The sensor matches the same player, target NPC, menu, and result ID. A successful sensor match consumes one result. This one-use behavior prevents the same selection from running again.

## Call a target-aware Java action

Use `InvokeRegisteredNpcAction` when another mod owns the behavior:

```json
{
  "Type": "InvokeRegisteredNpcAction",
  "Id": "inspect",
  "Label": "Inspect",
  "ActionId": "Example.InspectNpc",
  "Payload": {
    "mode": "full"
  }
}
```

The handler receives the player, menu key, option ID, payload, and live NPC target. The target is valid only during that handler call. See [Java API and Registered Actions](/mod/alecs-radial-menu/java-api-and-registered-actions).
