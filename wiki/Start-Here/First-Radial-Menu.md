---
title: "First Radial Menu"
order: 3
published: true
draft: false
---

# First Radial Menu

Parent: [Start Here](/mod/alecs-radial-menu/start-here) · [Home](/mod/alecs-radial-menu/home)

## Create the menu asset

Create `Server/RadialMenu/Menus/My_First_Menu.json` in your asset pack:

```json
{
  "Enabled": true,
  "ExecutionMode": "SelectAndRun",
  "DefaultOptionId": "help",
  "Options": [
    {
      "Type": "ExecuteCommand",
      "Id": "help",
      "Label": "Help",
      "Command": "/help"
    }
  ]
}
```

The menu key is `My_First_Menu`. Each menu must have between one and eight options. Each option must have a unique, nonblank `Id`.

## Bind the menu to an item

Add the item asset ID to the menu:

```json
"ItemIds": [
  "My_Radial_Item"
]
```

Then add this interaction to the item input that must open the menu:

```json
{
  "Type": "RadialMenuInteraction",
  "CommandId": "OpenMenu"
}
```

If you omit `MenuId`, the runtime uses the held item ID and the menu's `ItemIds` list. You can also set the menu directly:

```json
{
  "Type": "RadialMenuInteraction",
  "CommandId": "OpenMenu",
  "MenuId": "My_First_Menu"
}
```

## Test the result

Restart or reload the asset pack, equip the item, and use the bound input. Select `Help`. The player must run `/help` immediately because the menu uses `SelectAndRun`.

For an armed primary action, use `SelectAndArm` and bind a second item input to `CommandId: "ExecuteSelected"`. See [Option Types and Execution Modes](/mod/alecs-radial-menu/option-types-and-execution-modes).
