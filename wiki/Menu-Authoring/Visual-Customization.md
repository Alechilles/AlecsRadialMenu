---
title: "Visual Customization"
order: 4
published: true
draft: false
---

# Visual Customization

Parent: [Menu Authoring](/mod/alecs-radial-menu/menu-authoring) · [Home](/mod/alecs-radial-menu/home)

The `Visual` object controls the full wheel. An option can replace selected values with `VisualOverride`.

## Default geometry

| Field | Default |
| --- | ---: |
| `Geometry.OuterDiameterPx` | `640` |
| `Geometry.InnerDiameterPx` | `300` |
| `Geometry.LabelRadiusPx` | `234` |
| `Geometry.CenterDiameterPx` | `300` |
| `BorderThicknessPx` | `2` |
| `Label.FontSize` | `15` |

The inner diameter must be smaller than the outer diameter. The center diameter must not be larger than the inner diameter. The label radius must be between the inner and outer radii.

## State colors

The state names are `Default`, `Hover`, `Pressed`, `Selected`, and `Disabled`. Each state supports:

- `FillColor`
- `TextColor`
- `BorderColor`

Use hexadecimal colors. Alpha values can use the Hytale color suffix format, such as `#26343f(0.70)`.

```json
"Visual": {
  "States": {
    "Hover": {
      "FillColor": "#5d829f",
      "TextColor": "#ffffff",
      "BorderColor": "#30495b"
    }
  }
}
```

## Per-option overrides

`VisualOverride` supports `LabelFontSize` and partial `States` values.

```json
"VisualOverride": {
  "LabelFontSize": 18,
  "States": {
    "Selected": {
      "FillColor": "#4f8f54"
    }
  }
}
```

Missing override values inherit from the menu palette.

## Render modes and textures

`Texture` is the default render mode. An omitted texture prefix uses `RadialMenu/Default`. `Vector` remains a legacy and experimental mode.

Set a custom texture folder with:

```json
"Visual": {
  "RenderMode": "Texture",
  "TextureSet": {
    "Prefix": "MyMod/RadialMenu/Blue"
  }
}
```

The folder must contain the complete slice set with the expected file names. If the set is incomplete, the runtime logs a warning and uses `RadialMenu/Default`.

The repository includes `scripts/generate_rotated_radial_slices.py` for texture generation. Run the script help command before use so that you use its current arguments:

```text
python scripts/generate_rotated_radial_slices.py --help
```
