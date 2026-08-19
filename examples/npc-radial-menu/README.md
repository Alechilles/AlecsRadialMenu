# NPC radial menu example

Copy `Server/RadialMenu/Menus/Example_Npc.json` into an asset pack that depends on Alec's Radial Menu.

Merge these branches into the NPC role's `InteractionInstruction.Instructions` array:

```json
[
  {
    "Continue": true,
    "Sensor": {
      "Type": "Any"
    },
    "Actions": [
      {
        "Type": "SetInteractable",
        "Interactable": true
      }
    ]
  },
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
  },
  {
    "Sensor": {
      "Type": "RadialMenuResult",
      "MenuId": "Example_Npc",
      "ResultId": "stay"
    },
    "Actions": [
      {
        "Type": "SetFlag",
        "Name": "StayRequested",
        "SetTo": true
      }
    ]
  }
]
```

The role must declare `StayRequested` before it uses `SetFlag`. Replace the example states, flag, and registered action with values that exist in your role or mod.

`SetNpcState` changes the NPC state immediately. `EmitNpcResult` lets the role run its native action list. `InvokeRegisteredNpcAction` calls a Java handler registered through `RadialMenuApi`.
