# Alec's Radial Menu v2.0.0

## Summary
This release adds NPC-driven radial menu workflows. NPC instructions can open target-aware menus that change NPC state, emit named results, or run registered Java actions. The release also adds Hytale Update 6 support and a complete user and developer wiki.

## Added
- The `OpenRadialMenu` NPC action and the consuming `RadialMenuResult` sensor.
- Target-aware `SetNpcState`, `EmitNpcResult`, and `InvokeRegisteredNpcAction` menu options.
- Public Java API methods and handlers for NPC targets and target-aware actions.
- Shipped NPC role and menu examples, plus an Ice Staff option in the basic menu.
- Asset-editor tooltips for radial menu configuration.
- A complete wiki for setup, menu authoring, NPC integration, Java API use, compatibility, and troubleshooting.

## Changed
- NPC-only options remain visible but are disabled when the menu has no valid NPC target.
- Updated NPC integration for the Hytale Update 6 API.
- Migrated builds to the shared Gradle workspace and moved publishing to the central local release runner.

## Fixes
- Hardened NPC menu boundaries and delayed builder-context validation until the required runtime context exists.
- Declared externally set NPC states and localized the NPC interaction prompt.
- Qualified NPC interaction hint keys to prevent asset-key resolution errors.

## Compatibility
- Hytale Server: `>=0.5.7 <0.7.0 || >=0.6.0-pre.1 <0.6.0`
- Hytale modules: AssetModule and NPC
- Marketplace dependency: Alec's Telemetry 1.1.0

## Files
- `Alec's Radial Menu v2.0.0.jar`
