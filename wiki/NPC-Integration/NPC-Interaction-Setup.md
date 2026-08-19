---
title: "NPC Interaction Setup"
order: 2
published: true
draft: false
---

# NPC Interaction Setup

Parent: [NPC Integration](/mod/alecs-radial-menu/npc-integration) · [Home](/mod/alecs-radial-menu/home)

Add the interaction prompt and menu action to the role's `InteractionInstruction`.

## Make the NPC interactable

```json
{
  "Continue": true,
  "Sensor": {
    "Type": "Any"
  },
  "Actions": [
    {
      "Type": "SetInteractable",
      "Interactable": true,
      "Hint": "server.interactionHints.openRadialMenu"
    }
  ]
}
```

`Hint` must be a language key. Add the key to a server language file, for example `Server/Languages/en-US/server.lang`:

```text
interactionHints.openRadialMenu=Press [{key}] to open radial menu
```

The entry in `server.lang` omits the namespace prefix. The consuming `Hint` reference includes `server.`.

If the game shows the key text instead of the message, check the key spelling, asset-pack loading, and client cache. Then restart the client and server.

## Open the menu after interaction

```json
{
  "Sensor": {
    "Type": "HasInteracted"
  },
  "Actions": [
    {
      "Type": "OpenRadialMenu",
      "MenuId": "Example_Npc"
    }
  ]
}
```

`MenuId` is required for the NPC action and must match a menu asset key.

## Target lifetime

The menu keeps the NPC identity while the page is open. It resolves the live NPC only when the player selects an option. If the NPC despawns or becomes invalid, target-aware options do not run.

An NPC-opened menu always uses `SelectAndRun`. NPC-only options remain visible but disabled without a valid target.

Next: [States, Results, and NPC Actions](/mod/alecs-radial-menu/states-results-and-npc-actions)
