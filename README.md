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
- Execution modes:
  - `SelectAndArm` (menu click selects only; execute on `ExecuteSelected`)
  - `SelectAndRun` (menu click executes immediately and updates selection)

## `RadialMenuConfig` Fields
- `Enabled` (`true` default)
- `ItemIds[]` (menu can be auto-resolved by held item id)
- `ExecutionMode` (`SelectAndArm` default)
- `DefaultOptionId` (optional)
- `Options[]` (required, 1..8)

### Option Types
- `ExecuteCommand`
  - `Id`, optional `Label`/`LabelKey`, `Command`
- `InvokeRegisteredAction`
  - `Id`, optional `Label`/`LabelKey`, `ActionId`, optional `Payload`

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
