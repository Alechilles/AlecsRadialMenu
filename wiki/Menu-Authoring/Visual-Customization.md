---
title: "Visual Customization"
order: 4
published: true
draft: false
---

# Visual Customization

Parent: [Menu Authoring](/mod/alecs-radial-menu/menu-authoring) · [Home](/mod/alecs-radial-menu/home)

The `Visual` object controls the full wheel. An option can replace its font size with `VisualOverride`.

## Default geometry

| Field | Default |
| --- | ---: |
| `Geometry.OuterDiameterPx` | `640` |
| `Geometry.InnerDiameterPx` | `300` |
| `Geometry.LabelRadiusPx` | `234` |
| `Geometry.CenterDiameterPx` | `300` |
| `Label.FontSize` | `15` |

The inner diameter must be smaller than the outer diameter. The center diameter must not be larger than the inner diameter. The label radius must be between the inner and outer radii.

## Color support

The built-in wheel ships eight normal slice textures, eight brighter hover textures, and one center texture. Pressed reuses the normal slice texture.

Configured `States`, per-option color overrides, and `BorderThicknessPx` remain reserved for future Noesis UI support. They do not recolor the current wheel.

## Per-option overrides

`VisualOverride` currently supports `LabelFontSize`.

```json
"VisualOverride": {
  "LabelFontSize": 18
}
```

## Render modes and textures

`Texture` is the only active render mode. An omitted texture prefix uses `RadialMenu/Default`. Old `Vector` values fall back to `Texture`.

Set a custom texture folder with:

```json
"Visual": {
  "RenderMode": "Texture",
  "TextureSet": {
    "Prefix": "MyMod/RadialMenu/Blue"
  }
}
```

The folder must contain `CommandWheelCenterPanel.png` and `CommandWheelSlice0..7_{Default,Hover,Pressed}.png`. If the set is incomplete, the runtime logs a warning and uses `RadialMenu/Default`. Ring textures are not used.

Full-wheel textures that use a 640 by 640 canvas must also include `Cropped/CommandWheelSlice0..7_{Default,Hover,Pressed}.png`. These cropped files provide the visible button states and hit areas. Smaller slice textures do not need the `Cropped` folder.

The repository includes `scripts/generate_rotated_radial_slices.py` for texture generation. Run the script help command before use so that you use its current arguments:

```text
python scripts/generate_rotated_radial_slices.py --help
```
