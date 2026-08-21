# Alec's Radial Menu

Asset-driven radial menu framework mod for Hytale Server `0.5.x` and compatible `0.6` prereleases.

## Documentation

Use the [Alec's Radial Menu wiki](wiki/Home.md) for installation, menu authoring, NPC integration, Java API use, and troubleshooting.

## Features

- Define radial menus as assets under `Server/RadialMenu/Menus/*.json`.
- Show between 1 and 8 options on the built-in textured wheel.
- Bind a menu to one or more held item IDs, or open it directly through the Java API.
- Execute player commands, invoke actions registered by another mod, or run native Hytale `RootInteraction` assets.
- Open a target-aware menu from an NPC interaction and change that NPC through states, named results, or registered actions.
- Choose whether a slice runs immediately or becomes the item's armed primary action.
- Customize wheel geometry, labels, state colors, and texture sets, with per-option font and color overrides.
- Retain each player's selection independently for every menu until they disconnect.

## Native Interaction Wheels

`RunInteraction` options turn a radial menu into a wheel of Hytale interactions. A menu can mix interaction options with commands and registered mod actions.

There are two execution modes:

- `SelectAndArm`: choosing a slice stores it as the selected option. The item's `ExecuteSelected` binding then runs it through the active item interaction context. In the included example item, secondary use opens the wheel and primary use—normally left click—runs the armed option.
- `SelectAndRun`: choosing a slice starts a standalone interaction chain immediately and also remembers the selection.

### Interaction menu example

```json
{
  "Enabled": true,
  "ItemIds": [
    "Alec_Radial_Menu_Example"
  ],
  "ExecutionMode": "SelectAndArm",
  "DefaultOptionId": "left_swing",
  "Options": [
    {
      "Type": "RunInteraction",
      "Id": "left_swing",
      "Label": "Left Swing",
      "RootInteraction": "Root_Unarmed_Swing_Left",
      "InteractionType": "Primary"
    },
    {
      "Type": "ExecuteCommand",
      "Id": "help",
      "Label": "Help",
      "Command": "/help"
    }
  ]
}
```

The menu's identity is its asset key—the file/path key under `Server/RadialMenu/Menus`. `RadialMenuConfig` does not contain an `Id` field.

### Item binding example

Use `OpenMenu` on the input that should display the wheel and `ExecuteSelected` on the input that should run the armed option:

```json
{
  "Interactions": {
    "Primary": {
      "Interactions": [
        {
          "Type": "RadialMenuInteraction",
          "CommandId": "ExecuteSelected"
        }
      ]
    },
    "Secondary": {
      "Interactions": [
        {
          "Type": "RadialMenuInteraction",
          "CommandId": "OpenMenu"
        }
      ]
    }
  }
}
```

The complete example item is at `Server/Item/Items/Commands/Alec_Radial_Menu_Example.json`.

## `RadialMenuConfig` Fields
- `Enabled` (`true` default)
- `ItemIds[]` (menu can be auto-resolved by held item id)
- `ExecutionMode` (`SelectAndArm` default)
- `DefaultOptionId` (optional)
- `Options[]` (required, 1..8)
- `Visual` (optional, defaults to the built-in texture wheel)

### `Visual` Fields
- `RenderMode`: `Texture` (default) or `Vector` (legacy/experimental)
- `Geometry`:
  - `OuterDiameterPx`
  - `InnerDiameterPx`
  - `LabelRadiusPx`
  - `CenterDiameterPx`
- `BorderThicknessPx`
- `Label.FontSize`
- `States`:
  - `Default`, `Hover`, `Pressed`, `Selected`, `Disabled`
  - each state supports `FillColor`, `TextColor`, `BorderColor`
- `TextureSet`:
  - `Prefix` (optional custom texture set path using the same naming convention)
  - omitted `Prefix` uses the built-in `RadialMenu/Default` texture wheel

### Option Types

Every option supports `Id`, `Label` or `LabelKey`, optional `Feedback`, and optional `VisualOverride`.

- `ExecuteCommand`
  - Adds `Command`.
- `InvokeRegisteredAction`
  - Adds `ActionId` and optional string-to-string `Payload`.
- `RunInteraction`
  - Adds `RootInteraction` and optional `InteractionType`.
  - `RootInteraction` references a Hytale `RootInteraction` asset.
  - `InteractionType` defaults to `Primary`; `Equipped` is unsupported because it requires an equipment slot.
  - With `SelectAndRun`, selecting the option starts a standalone interaction chain immediately.
  - With `SelectAndArm`, selecting the option arms it and `ExecuteSelected` enters the root through the active item interaction context.
  - For an armed option, the active binding supplies the interaction type (normally `Primary`); the configured `InteractionType` applies to standalone execution.
- `SetNpcState`
  - Adds `State` and optional `SubState`.
  - Runs only when an NPC opened the menu.
- `EmitNpcResult`
  - Adds `ResultId`.
  - Sends a short-lived named result to the NPC instruction graph.
- `InvokeRegisteredNpcAction`
  - Adds `ActionId` and optional string-to-string `Payload`.
  - Calls a target-aware Java handler with the live NPC.

Example:

```json
{
  "Type": "RunInteraction",
  "Id": "left_swing",
  "Label": "Left Swing",
  "RootInteraction": "Root_Unarmed_Swing_Left",
  "InteractionType": "Primary"
}
```

### `Feedback` Fields (Option)

- `ChatMessage` and `HudMessage` send text after a successful option execution.
- `SoundEvent`, `ParticleSystem`, and `ParticleOffset` are reserved configuration fields; sound and particle playback are not implemented in the current runtime.

### `VisualOverride` Fields (Option)
- `LabelFontSize` (optional)
- `States` (optional partial state/color overrides)

## Interaction Usage

`RadialMenuInteraction` supports:

- `MenuId` (optional menu key override)
- `CommandId` (`OpenMenu` or `ExecuteSelected`)
- `ExecutionMode` (optional per-binding mode override)

If `MenuId` is omitted, the runtime resolves the menu from the held item's ID using the menu's `ItemIds` list.

## NPC Interaction Usage

Use the `OpenRadialMenu` action in an NPC `InteractionInstruction`:

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

An NPC-opened menu always uses `SelectAndRun`. It keeps a live target only until the player selects an option. If the NPC despawns first, the selection has no target effect. NPC-only options stay visible but disabled when a menu has no valid NPC target.

Use `EmitNpcResult` when the role must run a native action such as `SetFlag`, `State`, or an animation. Match and consume the result with `RadialMenuResult`:

```json
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
```

Results expire after five seconds. The sensor matches the same player, NPC, menu, and result, then consumes that result once. The mod ships a test role and menu. Run `/npc spawn Alec_Radial_Menu_Example_Npc`, then interact with the cow. See [`examples/npc-radial-menu`](examples/npc-radial-menu) for the test steps.

## API Usage
From another mod:

```java
RadialMenuApi api = RadialMenuMod.getApiInstance();
if (api != null) {
    AutoCloseable registration = api.registerActionHandler("Example.Ping", context -> {
        // custom action logic
        return true;
    });

    // Optional direct control:
    // api.openMenu(player, "menus/example");
    // api.openNpcMenu(player, "Example_Npc", npcRef, store);
    // api.executeSelected(player, "menus/example");
}
```

Register a target-aware NPC action when a menu option must call another mod:

```java
AutoCloseable registration = api.registerNpcActionHandler("Example.InspectNpc", context -> {
    var target = context.npcTarget();
    // Use target.reference(), target.npc(), and target.store() only during this call.
    return true;
});
```

Do not retain `RadialMenuNpcTarget`, its entity reference, its NPC component, or its store after the handler returns.

## Build
```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

## Maven Profiles
```powershell
# Build + install jar to Server/mods and UserData/Mods
.\mvnw.cmd -Pinstall-plugin package

# Build + install + launch Hytale server
.\mvnw.cmd -Prun-server package

# Use prerelease install path (can combine with profiles above)
.\mvnw.cmd -Dprerelease=true -Pinstall-plugin package
```

## Texture Slice Generator Script
Use `scripts/generate_rotated_radial_slices.py` to generate all 8 textured slice files from one source image.

```powershell
python scripts/generate_rotated_radial_slices.py `
  --input "C:\Users\22ale\Downloads\Ellipse.png" `
  --output-dir "target/radial-textures/EllipseTest" `
  --copy-core-from "src/main/resources/Common/UI/Custom/RadialMenu" `
  --mask-from "src/main/resources/Common/UI/Custom/RadialMenu"
```

Notes:
- Produces `CommandWheelSlice0..7_{Default,Hover,Pressed}.png`.
- Default rotation order is clockwise in 45-degree steps.
- Use `--base-angle` to tweak orientation if slice 0 needs an offset.
- `--mask-from` is recommended so generated textures keep the exact per-slice alpha silhouettes.

Use explicit exported segments instead of rotation:

```powershell
python scripts/generate_rotated_radial_slices.py `
  --segments-dir "C:\Users\22ale\Downloads\Untitled" `
  --segment-pattern "Segment {n}.png" `
  --output-dir "target/radial-textures/SegmentTest" `
  --copy-core-from "src/main/resources/Common/UI/Custom/RadialMenu" `
  --mask-from "src/main/resources/Common/UI/Custom/RadialMenu"
```

Notes:
- Segment mode maps `Segment 1..8` to slice indices `0..7`.
- Segment mode conforms each segment to the target slice canvas and applies optional mask clipping.
