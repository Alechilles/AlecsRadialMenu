# Alec's Radial Menu v0.2.0

## Summary
This release expands Alec's Radial Menu into a configurable visual framework and adds a polished shared-origin custom wheel style with aligned Figma-derived textures.

## Added
- Configurable radial menu visuals, including render modes, geometry, labels, state colors, and texture-set prefixes.
- Shared-origin custom wheel textures with focused hover states, cropped client-side hit targets, and a matching center panel.
- HStats integration and asset-pack coordination support.
- Validation coverage for visual configuration and texture completeness.

## Changed
- Updated the example radial menu to use the shared-origin custom wheel style.
- Moved texture generator examples to scratch output paths outside packaged resources.

## Fixed
- Updated compatibility for Hytale Update 5 APIs, including JOML vectors and player messaging.
- Reworked hover handling to use client-side button states, avoiding sticky hovered slices.

## Compatibility
- Hytale: 0.5.x
- Dependencies: None

## Artifact
- `Alec's Radial Menu v0.2.0.jar`
