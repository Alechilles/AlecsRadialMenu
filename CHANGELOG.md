# Changelog

## 0.2.0
- Added configurable radial menu visuals, including texture-set prefixes, geometry, state colors, labels, and texture/vector render modes.
- Added the default texture wheel with aligned Figma-exported slices, focused hover textures, cropped client-side hover hit targets, and a matching center panel.
- Added menu catalog validation coverage for visual configuration and texture completeness.
- Added HStats integration and asset-pack coordination support.
- Added a telemetry consent icon and stats descriptor so Alec's Radial Menu can opt into hosted usage summaries through the shared telemetry consent flow.
- Updated the example radial menu to use the new default texture wheel with practical starter commands.
- Updated hosted telemetry stats routing to the current Alec telemetry ingest endpoint used by the shared rollout.
- Fixed Hytale Update 5 compatibility for JOML vectors and player messaging.
- Fixed default wheel label alignment around the slice centers.
- Fixed the close button layout so its full visible area can hover and click.

## 0.1.0
- Initial standalone radial menu module.
- Added `RadialMenuConfig` asset family with asset-key menu IDs.
- Added `RadialMenuInteraction` item interaction (`OpenMenu`, `ExecuteSelected`).
- Added `RadialMenuApi` for API open/execute and custom action handler registration.
- Added built-in actions `ExecuteCommand` and `InvokeRegisteredAction`.
- Added two execution modes (`SelectAndArm`, `SelectAndRun`) with per-binding override support.
