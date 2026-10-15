from pathlib import Path
from PIL import Image, ImageDraw
out=Path(__file__).resolve().parents[1]/'src/main/resources/assets/compactreactors/textures/items'
out.mkdir(parents=True,exist_ok=True)
image=Image.new('RGBA',(16,16)); d=ImageDraw.Draw(image)
d.rectangle((4,1,11,3),fill=(162,173,153),outline=(32,43,35))
d.rectangle((3,4,12,14),fill=(83,184,207),outline=(32,43,35))
d.line((4,4,4,12),fill=(220,239,232))
d.rectangle((5,6,10,11),fill=(54,131,153))
d.line((5,12,10,12),fill=(164,201,170))
image.save(out/'coolant_cell.png')
