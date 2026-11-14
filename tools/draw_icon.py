"""Draw the CivicCraft logo on a 32-pixel grid."""

import argparse
from pathlib import Path

from PIL import Image, ImageDraw


def draw_icon():
    image = Image.new("RGB", (32, 32), "#26353a")
    draw = ImageDraw.Draw(image)
    draw.rectangle((1, 1, 30, 30), fill="#43585b")
    draw.ellipse((6, 24, 25, 28), fill="#354649")

    draw.polygon(
        [(8, 5), (19, 5), (22, 7), (22, 13), (20, 14), (20, 16),
         (24, 18), (24, 22), (22, 23), (22, 27), (19, 27), (18, 26),
         (18, 23), (13, 23), (13, 27), (9, 27), (8, 26), (8, 21),
         (7, 20), (7, 16), (8, 15)],
        fill="#30231f",
    )
    draw.rectangle((9, 6, 11, 17), fill="#d79255")
    draw.rectangle((17, 6, 19, 16), fill="#c8854d")
    draw.rectangle((10, 6, 19, 12), fill="#e9ad68")
    draw.line((11, 8, 18, 8), fill="#f2c185")
    draw.line((11, 11, 18, 11), fill="#bf7e49")
    draw.rectangle((20, 8, 20, 12), fill="#a36138")
    draw.rectangle((9, 21, 11, 25), fill="#c3854d")
    draw.rectangle((19, 21, 21, 25), fill="#a36138")
    draw.polygon([(10, 15), (17, 15), (22, 18), (22, 20), (12, 20), (8, 18), (8, 17)],
                 fill="#c8854d")
    draw.polygon([(10, 15), (17, 15), (21, 18), (12, 18), (9, 17)], fill="#e9ad68")
    draw.line((12, 20, 22, 20), fill="#a36138")
    return image


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--curseforge", type=Path, help="Also write a 512px PNG")
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    image = draw_icon()
    image.resize((128, 128), Image.Resampling.NEAREST).save(
        root / "src/main/resources/assets/civiccraft/icon.png"
    )
    if args.curseforge:
        image.resize((512, 512), Image.Resampling.NEAREST).save(args.curseforge)


if __name__ == "__main__":
    main()
