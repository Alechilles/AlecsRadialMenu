---
title: "Menu Asset Reference"
order: 2
published: true
draft: false
---

# Menu Asset Reference

Parent: [Menu Authoring](/mod/alecs-radial-menu/menu-authoring) · [Home](/mod/alecs-radial-menu/home)

Place menu assets under `Server/RadialMenu/Menus`. The asset key comes from the file name and path.

## Root fields

| Field | Required | Description |
| --- | --- | --- |
| `Enabled` | No | Enables the menu. Default: `true`. |
| `ItemIds` | No | Item asset IDs that can resolve this menu. |
| `ExecutionMode` | No | `SelectAndArm` or `SelectAndRun`. Default: `SelectAndArm`. |
| `DefaultOptionId` | No | Option selected when the menu opens. The first valid option is used if this field is absent. |
| `Options` | Yes | Ordered list of one to eight options. |
| `Visual` | No | Menu geometry, labels, colors, and texture settings. |

A menu is skipped if its configuration is invalid. Option IDs are case-insensitive during lookup and must be unique.

## Fields shared by all options

| Field | Required | Description |
| --- | --- | --- |
| `Type` | Yes | Selects the option behavior. |
| `Id` | Yes | Stable option ID. |
| `Label` | No | Literal text. It takes priority over `LabelKey`. |
| `LabelKey` | No | Language key used when `Label` is blank. |
| `Feedback` | No | Text sent after successful execution. |
| `VisualOverride` | No | Per-option font and partial color overrides. |

If both label fields are blank, the runtime uses the option ID as the displayed label.

## Localized labels

Add a key to a language file, for example:

```text
radialmenu.myMenu.help=Help
```

Then use it in the option:

```json
{
  "Type": "ExecuteCommand",
  "Id": "help",
  "LabelKey": "radialmenu.myMenu.help",
  "Command": "/help"
}
```

## Feedback

`ChatMessage` and `HudMessage` send raw text after an option succeeds.

```json
"Feedback": {
  "ChatMessage": "The action completed.",
  "HudMessage": "Done"
}
```

`SoundEvent`, `ParticleSystem`, and `ParticleOffset` are reserved fields. The version 1 runtime does not play or spawn them.

## Item interaction fields

`RadialMenuInteraction` supports:

| Field | Required | Description |
| --- | --- | --- |
| `CommandId` | No | `OpenMenu` or `ExecuteSelected`. If absent, the interaction opens the menu. |
| `MenuId` | No | Direct menu key. If absent, the runtime resolves `ItemIds`. |
| `ExecutionMode` | No | Per-binding mode override. |

Use [Option Types and Execution Modes](/mod/alecs-radial-menu/option-types-and-execution-modes) for fields that are specific to one option type.
