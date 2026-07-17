# Alec's Radial Menu

Standalone radial menu framework mod for Hytale.

## V1 Scope
- Asset-driven radial menus at `Server/RadialMenu/Menus/*.json`
- Menu identity is the asset key (file/path key). `RadialMenuConfig` has no `Id` field.
- Item interaction entrypoint: `Type: "RadialMenuInteraction"`
- API entrypoint: `RadialMenuApi`
- Built-in option action types:
  - `ExecuteCommand`
  - `InvokeRegisteredAction`
  - `RunInteraction`
- Execution modes:
  - `SelectAndArm` (menu click selects only; execute on `ExecuteSelected`)
  - `SelectAndRun` (menu click executes immediately and updates selection)

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
- `ExecuteCommand`
  - `Id`, optional `Label`/`LabelKey`, `Command`, optional `VisualOverride`
- `InvokeRegisteredAction`
  - `Id`, optional `Label`/`LabelKey`, `ActionId`, optional `Payload`, optional `VisualOverride`
- `RunInteraction`
  - `Id`, optional `Label`/`LabelKey`, `RootInteraction`, optional `InteractionType`, optional `VisualOverride`
  - `RootInteraction` references a Hytale `RootInteraction` asset.
  - `InteractionType` defaults to `Primary`; `Equipped` is unsupported because it requires an equipment slot.
  - With `SelectAndRun`, selecting the option starts a standalone interaction chain immediately.
  - With `SelectAndArm`, selecting the option arms it and `ExecuteSelected` enters the root through the active item interaction context.
  - For an armed option, the active binding supplies the interaction type (normally `Primary`); the configured `InteractionType` applies to standalone execution.

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

### `VisualOverride` Fields (Option)
- `LabelFontSize` (optional)
- `States` (optional partial state/color overrides)

## Interaction Usage
`RadialMenuInteraction` supports:
- `MenuId` (optional menu key override)
- `CommandId` (`OpenMenu` or `ExecuteSelected`)
- `ExecutionMode` (optional per-binding mode override)

Example item is included:
- `Server/Item/Items/Commands/Alec_Radial_Menu_Example.json`

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
    // api.executeSelected(player, "menus/example");
}
```

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
