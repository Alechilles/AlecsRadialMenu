# Alec's Radial Menu v1.0.0

## Summary
This release adds native Hytale interaction execution to radial options, allowing menus to run interactions immediately or arm them for the next primary click. It also fixes armed-interaction timing and updates the telemetry descriptor for the current hosted telemetry flow.

## Added
- Native `RunInteraction` radial options for executing Hytale `RootInteraction` assets immediately or arming them for the radial item's next primary click.
- Project licensing information and distribution-page icons.

## Changed
- Moved the telemetry descriptor to `Server/Telemetry/project.json` and updated it to the current stats descriptor schema and hosted endpoint behavior.

## Fixes
- Fixed armed interactions failing when their Hytale interaction context was used after the active tick ended.
- Fixed the Git Bash Maven wrapper so release builds use Java from `JAVA_HOME` or `PATH`.

## Compatibility
- Hytale: 0.5.x
- Dependencies: None

## Files
- `Alec's Radial Menu v1.0.0.jar`
