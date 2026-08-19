# Changelog

## 2.0.0
- Added NPC-opened radial menus with target-aware options for state changes, named instruction results, and registered Java actions.
- Added the `OpenRadialMenu` NPC action and the consuming `RadialMenuResult` sensor, with shipped NPC role and menu examples.
- Added target-aware NPC methods and handlers to the public Java API.
- Added availability rules that keep NPC-only options visible but disabled when no valid NPC target exists.
- Added asset-editor tooltips for radial menu configuration fields.
- Added a complete wiki for installation, menu authoring, NPC integration, Java API use, compatibility, and troubleshooting.
- Added an Ice Staff interaction to the basic example menu.
- Updated NPC integration for the Hytale Update 6 API and declared the required NPC states.
- Fixed NPC menu boundary handling, deferred builder-context validation, localized interaction prompts, and qualified NPC hint keys.
- Migrated builds to the shared Gradle workspace and moved release publishing to the central local publisher.

## 1.0.0
- Fixed armed radial interactions failing because their active Hytale interaction context was used after its tick ended.
- Added native `RunInteraction` radial options that can execute Hytale `RootInteraction` assets immediately or arm them for the radial item's next primary click.
- Moved the telemetry descriptor to `Server/Telemetry/project.json`, updated it to the current stats descriptor schema, and removed dev endpoint overrides so Alec's Telemetry uses its default hosted endpoint.
- Fixed the Git Bash Maven wrapper to resolve Java from `JAVA_HOME` or `PATH` instead of a removed JDK installation.
- Added project licensing information.
- Added project icons for distribution pages.

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
