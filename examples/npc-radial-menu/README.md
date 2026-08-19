# NPC radial menu example

The mod ships a loadable example menu and NPC role:

- `Server/RadialMenu/Menus/Example_Npc.json`
- `Server/NPC/Roles/Examples/Alec_Radial_Menu_Example_Npc.json`

Install the mod, enter a world with permission to use NPC commands, and run:

```text
/npc spawn Alec_Radial_Menu_Example_Npc
```

Interact with the cow to open `Example_Npc`. The menu shows three paths:

- **Sleep** uses `SetNpcState` to set the role state to `Sleep`.
- **Alert** uses `SetNpcState` to set the role state to `Alerted`.
- **Wake and Mark** emits `wake_and_mark`. The role's `RadialMenuResult` sensor consumes it, sets `RadialMenuExampleMarked`, and returns the role to `Idle`.

The cow animation and display name show each state. After **Wake and Mark**, the name includes `(marked)` to confirm that the native `SetFlag` action ran.

Hytale allocates a flag slot when a role uses `SetFlag` or a flag sensor. A separate flag declaration is not required. Replace the example states and flag name with values used by your own role.
