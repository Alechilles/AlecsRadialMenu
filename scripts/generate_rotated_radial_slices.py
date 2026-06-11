#!/usr/bin/env python3
"""
Generate CommandWheelSlice textures either by:
1) rotating a base image for each slice index, or
2) importing explicit Segment 1..8 images.

Outputs files named:
  CommandWheelSlice{0..7}_{Default|Hover|Pressed}.png

Optionally copies required texture-mode core files:
  CommandWheelCenterPanel.png, CommandWheelRingInner.png, CommandWheelRingOuter.png
"""

from __future__ import annotations

import argparse
import shutil
from pathlib import Path

from PIL import Image, ImageChops


DEFAULT_TARGET_SIZES = [
    (217, 142),  # 0
    (207, 207),  # 1
    (142, 217),  # 2
    (207, 207),  # 3
    (217, 142),  # 4
    (207, 207),  # 5
    (142, 217),  # 6
    (207, 207),  # 7
]

NEW_LAYOUT_TARGET_SIZES = [
    (238, 257),  # 0
    (350, 350),  # 1
    (257, 238),  # 2
    (350, 350),  # 3
    (238, 257),  # 4
    (350, 350),  # 5
    (257, 238),  # 6
    (350, 350),  # 7
]

CORE_TEXTURE_FILES = (
    "CommandWheelCenterPanel.png",
    "CommandWheelRingInner.png",
    "CommandWheelRingOuter.png",
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate radial slice textures from one image or explicit segment exports."
    )
    parser.add_argument("--input", help="Base image for Default state (rotation mode).")
    parser.add_argument("--hover-input", help="Optional image for Hover state (defaults to --input).")
    parser.add_argument("--pressed-input", help="Optional image for Pressed state (defaults to --input).")
    parser.add_argument("--segments-dir", help="Directory with explicit segment files (segment mode).")
    parser.add_argument(
        "--segment-pattern",
        default="Segment {n}.png",
        help="Filename pattern inside --segments-dir. Supports {n}=1..8 and {i}=0..7.",
    )
    parser.add_argument("--output-dir", required=True, help="Output directory for generated textures.")
    parser.add_argument(
        "--direction",
        choices=("cw", "ccw"),
        default="cw",
        help="Rotation direction from slice 0 to slice 7. Default: cw",
    )
    parser.add_argument(
        "--base-angle",
        type=float,
        default=0.0,
        help="Angle offset (degrees) applied to slice 0. Default: 0",
    )
    parser.add_argument(
        "--preserve-scale",
        action="store_true",
        help="Do not scale per-slice; rotate then center crop/pad to target size.",
    )
    parser.add_argument(
        "--ignore-existing-sizes",
        action="store_true",
        help="Use built-in target sizes instead of reading existing slice sizes from output dir.",
    )
    parser.add_argument(
        "--size-profile",
        choices=("legacy", "newlayout"),
        default="legacy",
        help="Target size profile when not using --uniform-size. Default: legacy",
    )
    parser.add_argument(
        "--uniform-size",
        help="Optional fixed output size WxH for all 8 slices (for example 350x350).",
    )
    parser.add_argument(
        "--copy-core-from",
        help="Optional directory to copy center/ring textures from (for texture-set completeness).",
    )
    parser.add_argument(
        "--mask-from",
        help="Optional directory containing CommandWheelSlice{0..7}_Default.png alpha masks.",
    )
    return parser.parse_args()


def parse_uniform_size(raw: str | None) -> tuple[int, int] | None:
    if raw is None:
        return None
    token = raw.strip().lower().replace(" ", "")
    if "x" not in token:
        raise ValueError(f"Invalid --uniform-size '{raw}'. Expected format WxH.")
    w_token, h_token = token.split("x", 1)
    width = int(w_token)
    height = int(h_token)
    if width <= 0 or height <= 0:
        raise ValueError(f"Invalid --uniform-size '{raw}'. Width/height must be > 0.")
    return (width, height)


def resolve_target_sizes(output_dir: Path, ignore_existing_sizes: bool, size_profile: str) -> list[tuple[int, int]]:
    if size_profile == "newlayout":
        sizes = list(NEW_LAYOUT_TARGET_SIZES)
    else:
        sizes = list(DEFAULT_TARGET_SIZES)
    if ignore_existing_sizes:
        return sizes
    for index in range(8):
        candidate = output_dir / f"CommandWheelSlice{index}_Default.png"
        if candidate.exists():
            with Image.open(candidate) as image:
                sizes[index] = image.size
    return sizes


def rotate_fit(source: Image.Image, angle_deg: float, target_size: tuple[int, int]) -> Image.Image:
    rotated = source.rotate(angle_deg, resample=Image.Resampling.BICUBIC, expand=True)
    target_w, target_h = target_size
    if target_w <= 0 or target_h <= 0:
        raise ValueError(f"Invalid target size: {target_size}")

    scale = min(target_w / rotated.width, target_h / rotated.height)
    scaled_w = max(1, int(round(rotated.width * scale)))
    scaled_h = max(1, int(round(rotated.height * scale)))
    if (scaled_w, scaled_h) != rotated.size:
        rotated = rotated.resize((scaled_w, scaled_h), resample=Image.Resampling.LANCZOS)

    canvas = Image.new("RGBA", (target_w, target_h), (0, 0, 0, 0))
    x = (target_w - scaled_w) // 2
    y = (target_h - scaled_h) // 2
    canvas.alpha_composite(rotated, (x, y))
    return canvas


def rotate_preserve_scale(source: Image.Image, angle_deg: float, target_size: tuple[int, int]) -> Image.Image:
    rotated = source.rotate(angle_deg, resample=Image.Resampling.BICUBIC, expand=True)
    target_w, target_h = target_size
    if target_w <= 0 or target_h <= 0:
        raise ValueError(f"Invalid target size: {target_size}")

    work = rotated
    if work.width > target_w or work.height > target_h:
        left = max(0, (work.width - target_w) // 2)
        top = max(0, (work.height - target_h) // 2)
        right = left + min(target_w, work.width)
        bottom = top + min(target_h, work.height)
        work = work.crop((left, top, right, bottom))

    canvas = Image.new("RGBA", (target_w, target_h), (0, 0, 0, 0))
    x = (target_w - work.width) // 2
    y = (target_h - work.height) // 2
    canvas.alpha_composite(work, (x, y))
    return canvas


def fit_cover(source: Image.Image, target_size: tuple[int, int]) -> Image.Image:
    target_w, target_h = target_size
    if target_w <= 0 or target_h <= 0:
        raise ValueError(f"Invalid target size: {target_size}")

    scale = max(target_w / source.width, target_h / source.height)
    scaled_w = max(1, int(round(source.width * scale)))
    scaled_h = max(1, int(round(source.height * scale)))
    resized = source.resize((scaled_w, scaled_h), resample=Image.Resampling.LANCZOS)

    x = (scaled_w - target_w) // 2
    y = (scaled_h - target_h) // 2
    return resized.crop((x, y, x + target_w, y + target_h))


def apply_mask_if_present(rendered: Image.Image, index: int, mask_from: Path | None) -> Image.Image:
    if mask_from is None:
        return rendered
    mask_file = mask_from / f"CommandWheelSlice{index}_Default.png"
    if not mask_file.exists():
        return rendered
    with Image.open(mask_file).convert("RGBA") as mask_image:
        if mask_image.size != rendered.size:
            mask_image = mask_image.resize(rendered.size, resample=Image.Resampling.LANCZOS)
        rendered_alpha = rendered.getchannel("A")
        mask_alpha = mask_image.getchannel("A")
        rendered.putalpha(ImageChops.multiply(rendered_alpha, mask_alpha))
    return rendered


def generate_state(
    source_path: Path,
    output_dir: Path,
    state_suffix: str,
    target_sizes: list[tuple[int, int]],
    base_angle: float,
    direction: str,
    preserve_scale: bool,
    mask_from: Path | None,
) -> None:
    sign = -1.0 if direction == "cw" else 1.0
    with Image.open(source_path).convert("RGBA") as source:
        for index in range(8):
            angle = base_angle + sign * index * 45.0
            rendered = (
                rotate_preserve_scale(source, angle, target_sizes[index])
                if preserve_scale
                else rotate_fit(source, angle, target_sizes[index])
            )
            rendered = apply_mask_if_present(rendered, index, mask_from)
            out_path = output_dir / f"CommandWheelSlice{index}_{state_suffix}.png"
            rendered.save(out_path)


def generate_from_segments(
    segments_dir: Path,
    segment_pattern: str,
    output_dir: Path,
    mask_from: Path | None,
) -> None:
    for index in range(8):
        filename = segment_pattern.format(n=index + 1, i=index)
        segment_path = segments_dir / filename
        if not segment_path.exists():
            raise FileNotFoundError(f"Segment file not found: {segment_path}")

        with Image.open(segment_path).convert("RGBA") as segment:
            rendered = segment.copy()
            rendered = apply_mask_if_present(rendered, index, mask_from)
            for state_suffix in ("Default", "Hover", "Pressed"):
                out_path = output_dir / f"CommandWheelSlice{index}_{state_suffix}.png"
                rendered.save(out_path)


def copy_core_textures(copy_from: Path, output_dir: Path) -> None:
    for filename in CORE_TEXTURE_FILES:
        src = copy_from / filename
        if not src.exists():
            print(f"[warn] missing core texture: {src}")
            continue
        shutil.copy2(src, output_dir / filename)


def main() -> int:
    args = parse_args()

    default_input = Path(args.input) if args.input else None
    hover_input = Path(args.hover_input) if args.hover_input else default_input
    pressed_input = Path(args.pressed_input) if args.pressed_input else default_input
    segments_dir = Path(args.segments_dir) if args.segments_dir else None
    output_dir = Path(args.output_dir)
    mask_from = Path(args.mask_from) if args.mask_from else None

    if segments_dir is None and default_input is None:
        raise ValueError("Provide either --input (rotation mode) or --segments-dir (segment mode).")
    if segments_dir is not None and default_input is not None:
        raise ValueError("Use either --input or --segments-dir, not both.")

    if segments_dir is not None:
        if not segments_dir.exists():
            raise FileNotFoundError(f"Segments directory not found: {segments_dir}")
    else:
        for path in (default_input, hover_input, pressed_input):
            if path is None or not path.exists():
                raise FileNotFoundError(f"Input image not found: {path}")

    output_dir.mkdir(parents=True, exist_ok=True)
    uniform_size = parse_uniform_size(args.uniform_size)
    target_sizes = (
        [uniform_size for _ in range(8)]
        if uniform_size is not None
        else resolve_target_sizes(output_dir, args.ignore_existing_sizes, args.size_profile)
    )

    if segments_dir is not None:
        generate_from_segments(segments_dir, args.segment_pattern, output_dir, mask_from)
    else:
        generate_state(
            default_input,
            output_dir,
            "Default",
            target_sizes,
            args.base_angle,
            args.direction,
            args.preserve_scale,
            mask_from,
        )
        generate_state(
            hover_input,
            output_dir,
            "Hover",
            target_sizes,
            args.base_angle,
            args.direction,
            args.preserve_scale,
            mask_from,
        )
        generate_state(
            pressed_input,
            output_dir,
            "Pressed",
            target_sizes,
            args.base_angle,
            args.direction,
            args.preserve_scale,
            mask_from,
        )

    if args.copy_core_from:
        copy_core_textures(Path(args.copy_core_from), output_dir)

    print(f"Generated slice textures in: {output_dir}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
