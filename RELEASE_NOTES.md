# Alec's Radial Menu v2.0.3

## Summary
This hotfix removes unused UI textures and keeps the built-in hover state clear and bright.

## Changed
- Reduced the built-in wheel to eight normal slices, eight hover slices, and one center texture.
- Reused each normal slice for the pressed state.
- Disabled experimental vector and custom color rendering until Noesis UI is available. Existing `Vector` configurations fall back to the default texture wheel.

## Fixed
- Restored separate bright hover textures after the runtime tint was too subtle.

## Compatibility
- Hytale Server: `>=0.5.0 <0.7.0`
- Hytale modules: AssetModule and NPC
- Marketplace dependency: Alec's Telemetry 1.2.2

## Files
- `Alec's Radial Menu v2.0.3.jar`
