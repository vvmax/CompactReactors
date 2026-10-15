"""Derive the advanced block's cyan/blue metal palette from the existing pixel textures."""
from pathlib import Path
from PIL import Image

out=Path(__file__).resolve().parents[1]/'src/main/resources/assets/compactreactors/textures/blocks'
for material in ('frame','side','front','top','core','base'):
    image=Image.open(out/('reactor_'+material+'.png')).convert('RGB')
    colors=[]
    for r,g,b in image.getdata():
        if g-r>15 and g-b>30:
            colors.append((int(g*.40),int(g*.90),min(255,int(g*1.08))))
        else:
            colors.append((int(r*.85),int(g*.98),min(255,int(b*1.12)+8)))
    image.putdata(colors)
    image.save(out/('advanced_reactor_'+material+'.png'))
print('Generated six cyan/blue advanced reactor textures')
