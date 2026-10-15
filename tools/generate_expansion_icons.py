"""Pixel-aligned module sprites using the existing reactor's metal/circuit palette."""
from pathlib import Path
from PIL import Image, ImageDraw

out = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/compactreactors/textures/items'
out.mkdir(parents=True, exist_ok=True)
for tier, accent in [(1, (181, 215, 107)), (2, (227, 189, 98))]:
    image = Image.new('RGBA', (16, 16))
    d = ImageDraw.Draw(image)
    d.rectangle((2, 1, 13, 13), fill=(32, 44, 37), outline=(154, 173, 160))
    d.line((3, 2, 12, 2), fill=(220, 228, 211))
    for x in (3, 6, 9, 12):
        d.line((x, 14, x, 15), fill=(198, 166, 67))
    d.rectangle((5, 4, 10, 10), fill=accent, outline=(82, 97, 77))
    for x in (3, 12):
        d.line((x, 4, x, 11), fill=(105, 140, 76))
        d.line((x, 7, 5 if x == 3 else 10, 7), fill=accent)
    if tier == 1:
        d.line((7, 5, 7, 9), fill=(32, 44, 37))
    else:
        d.line((7, 5, 7, 9), fill=(32, 44, 37))
        d.line((9, 5, 9, 9), fill=(32, 44, 37))
    image.save(out / ('expansion_%d.png' % tier))
print('Generated two 16x16 expansion module icons')
