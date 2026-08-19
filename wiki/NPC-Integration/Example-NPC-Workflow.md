---
title: "Example NPC Workflow"
order: 4
published: true
draft: false
---

# Example NPC Workflow

Parent: [NPC Integration](/mod/alecs-radial-menu/npc-integration) · [Home](/mod/alecs-radial-menu/home)

The mod includes these test assets:

- `Server/RadialMenu/Menus/Example_Npc.json`
- `Server/NPC/Roles/Examples/Alec_Radial_Menu_Example_Npc.json`
- `Server/Languages/en-US/server.lang`

## Spawn and test

Run this command in a test world with NPC command permission:

```text
/npc spawn Alec_Radial_Menu_Example_Npc
```

Interact with the cow. The prompt must say `Press [key] to open radial menu`, with the current interaction key in place of `[key]`.

The menu has three paths:

| Option | Path | Expected result |
| --- | --- | --- |
| `Sleep` | `SetNpcState` | The role enters `Sleep`; its name and animation change. |
| `Alert` | `SetNpcState` | The role enters `Alerted`; its name and animation change. |
| `Wake and Mark` | `EmitNpcResult` | The role sets `RadialMenuExampleMarked` and returns to `Idle`. |

After `Wake and Mark`, the display name includes `(marked)`. This confirms that the role's native `SetFlag` action ran.

Hytale allocates the flag slot because the role uses the flag in `SetFlag` and a flag sensor. Replace the example state names, result ID, and flag name with values from your role.

If the example does not load, use [Troubleshooting](/mod/alecs-radial-menu/troubleshooting).
