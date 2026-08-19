---
title: "Troubleshooting"
order: 3
published: true
draft: false
---

# Troubleshooting

Parent: [Reference Library](/mod/alecs-radial-menu/reference-library) · [Home](/mod/alecs-radial-menu/home)

## The interaction prompt shows a raw language key

Use the fully qualified key in the role:

```json
"Hint": "server.interactionHints.openRadialMenu"
```

Define the entry without the namespace prefix in `Server/Languages/en-US/server.lang`:

```text
interactionHints.openRadialMenu=Press [{key}] to open radial menu
```

Check spelling and letter case. Restart the client and server after you change language assets.

## The NPC role reports a missing state setter

If a state is changed only through `SetNpcState`, add `IgnoreMissingSetState: true` to the matching state sensor. Do not add a fake state action only to satisfy role validation.

## A state option reports failure

Check the exact `State` and `SubState` names in the NPC role. The runtime rejects unknown names. Also confirm that the NPC is still alive and valid when the player selects the option.

## NPC-only options are disabled

This is expected when the menu was opened without `OpenRadialMenu`, or when the target NPC became invalid. Open the menu from the NPC interaction again.

## The menu does not open

1. Check the server log for `RadialMenu skipped menu`.
2. Confirm that the menu has one to eight valid options.
3. Confirm that `Enabled` is not `false`.
4. Check `MenuId`, or check that the held item ID is present in `ItemIds`.
5. Remove duplicate old mod JARs and restart the server.

## `ExecuteSelected` does not run the expected option

Confirm that both item interactions resolve the same menu. If no slice was selected, the runtime uses `DefaultOptionId` and then the first valid option.

For `RunInteraction`, remember that an armed action uses the active item context. A standalone `SelectAndRun` action uses the configured `InteractionType`.

## A registered action does not run

Check that the other mod registered the exact `ActionId` after Alec's Radial Menu setup. Use `listActionIds()` or `listNpcActionIds()` to inspect active IDs.

The handler must return `true` after successful work. A missing handler, thrown exception, or `false` return makes the option fail.

## A named NPC result does not match

Check all of these values:

- The menu option `ResultId`.
- The sensor `ResultId`.
- The sensor `MenuId`.
- The current interaction player.

The sensor must match within five seconds. It consumes a successful result once.

## A custom texture set falls back to the default

The custom prefix is missing one or more required slice textures. Check the server warning, compare the complete naming set, and regenerate the files with `scripts/generate_rotated_radial_slices.py`.
