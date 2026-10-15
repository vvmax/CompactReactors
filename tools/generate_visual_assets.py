from pathlib import Path
from PIL import Image,ImageDraw,ImageFilter,ImageFont
import math,re
root=Path.cwd();out=root/'src/main/resources/assets/compactreactors/textures/blocks';out.mkdir(parents=True,exist_ok=True)
steel=(162,173,153);light=(201,208,184);dark=(32,43,35);edge=(82,97,77);green=(164,201,67);yellow=(198,166,67)
def image():return Image.new('RGB',(32,32),steel)
def bolt(d,x,y):
 d.rectangle((x,y,x+2,y+2),fill=dark);d.point((x,y),fill=light);d.line((x,y+1,x+1,y+2),fill=(107,121,95))
im=image();d=ImageDraw.Draw(im)
d.rectangle((0,0,31,31),outline=dark,width=1);d.line((1,1,30,1),fill=light);d.line((1,1,1,30),fill=light);d.line((2,30,30,30),fill=edge);d.line((30,2,30,30),fill=edge)
for x in (1,28):
 for y in (1,28):bolt(d,x,y)
for y in (5,26):d.line((5,y,26,y),fill=(145,159,134))
im.save(out/'reactor_frame.png')
im=image();d=ImageDraw.Draw(im);d.rectangle((3,3,28,28),fill=edge);d.rectangle((4,4,27,27),fill=dark)
for y in range(7,26,4):
 d.rectangle((7,y,24,y+1),fill=(13,24,17));d.line((7,y+2,24,y+2),fill=(106,122,93))
for x in (4,25):
 for y in (4,25):bolt(d,x,y)
im.save(out/'reactor_side.png')
im=Image.new('RGB',(32,32),dark);d=ImageDraw.Draw(im)
d.rectangle((5,5,26,26),fill=(85,106,72));d.rectangle((6,6,25,25),fill=(12,28,18))
for x in range(8,25,4):d.line((x,8,x,23),fill=(31,59,34))
for y in range(8,25,4):d.line((8,y,23,y),fill=(31,59,34))
for x in (6,24):
 for y in (6,24):d.point((x,y),fill=(166,190,117))
for x in range(8,24):
 d.point((x,25),fill=yellow if (x//3)%2==0 else dark)
d.rectangle((22,7,23,8),fill=green)
im.save(out/'reactor_front.png')
im=image();d=ImageDraw.Draw(im)
d.rectangle((3,3,28,28),outline=edge,width=1);d.rectangle((6,6,25,25),fill=dark)
d.rectangle((7,7,24,24),outline=(105,129,79),width=1);d.rectangle((9,9,22,22),fill=(24,43,26))
for x in (11,15,19):
 d.rectangle((x,10,x+1,21),fill=green);d.line((x,10,x,21),fill=(218,235,119))
for y in range(9,24,3):
 d.line((4,y,5,y),fill=dark);d.line((26,y,27,y),fill=dark)
for x in (3,26):
 for y in (3,26):bolt(d,x,y)
im.save(out/'reactor_top.png')
im=Image.new('RGB',(32,32));px=im.load()
for y in range(32):
 for x in range(32):
  band=(x%6 in (1,2));v=18 if band else 0
  px[x,y]=(143+v,184+v,47+v) if y%8 not in (0,7) else (70,98,34)
im.save(out/'reactor_core.png')
im=Image.new('RGB',(32,32),(38,50,40));d=ImageDraw.Draw(im)
for x in range(2,32,4):d.line((x,0,x,31),fill=(48,62,48))
for y in range(2,32,4):d.line((0,y,31,y),fill=(48,62,48))
im.save(out/'reactor_base.png')
# Preview is rendered from the same cuboids and UV coordinates as the Java renderer.
text=(root/'src/main/java/dev/compactreactors/ReactorShape.java').read_text()
materials={'FRAME':0,'SIDE':1,'FRONT':2,'TOP':3,'CORE':4,'BASE':5}
parts=[]
for m in re.finditer(r'\{([0-9., ]+),(FRAME|SIDE|FRONT|TOP|CORE|BASE)\}',text):
 vals=[float(v) for v in m.group(1).split(',')];parts.append((vals,materials[m.group(2)]))
textures=[Image.open(out/('reactor_'+name+'.png')).convert('RGB') for name in ['frame','side','front','top','core','base']]
W=640;canvas=Image.new('RGB',(W,W),(21,29,24));pixels=canvas.load();depth=[-1e10]*(W*W)
def norm(v):
 l=math.sqrt(sum(x*x for x in v));return tuple(x/l for x in v)
def dot(a,b):return sum(x*y for x,y in zip(a,b))
view=norm((2.8,2.1,-3.4));right=norm((-view[2],0,view[0]));up=(-view[1]*right[2],right[2]*view[0]-right[0]*view[2],view[1]*right[0])
light=norm((.4,.8,-.6));scale=340
for vals,material in parts:
 x0,y0,z0,x1,y1,z1=[v/16 for v in vals]
 faces=[((1,0,0),[(x1,y0,z0),(x1,y1,z0),(x1,y1,z1),(x1,y0,z1)]),((-1,0,0),[(x0,y0,z1),(x0,y1,z1),(x0,y1,z0),(x0,y0,z0)]),((0,1,0),[(x0,y1,z0),(x0,y1,z1),(x1,y1,z1),(x1,y1,z0)]),((0,-1,0),[(x0,y0,z1),(x0,y0,z0),(x1,y0,z0),(x1,y0,z1)]),((0,0,-1),[(x0,y0,z0),(x0,y1,z0),(x1,y1,z0),(x1,y0,z0)]),((0,0,1),[(x1,y0,z1),(x1,y1,z1),(x0,y1,z1),(x0,y0,z1)])]
 for normal,vertices in faces:
  if dot(normal,view)<=0:continue
  points=[]
  for v in vertices:
   rel=tuple(c-.5 for c in v);xx=W/2+scale*dot(rel,right);yy=W/2-scale*dot(rel,up);zz=dot(rel,view)
   uv=(v[2],1-v[1]) if normal[0] else (v[0],v[2]) if normal[1] else (v[0],1-v[1])
   points.append((xx,yy,zz,*uv))
  shade=.55+.45*max(0,dot(normal,light));tex=textures[material].load()
  for tri in ((0,1,2),(0,2,3)):
   a,b,c=[points[i] for i in tri];den=(b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
   if abs(den)<1e-8:continue
   for y in range(max(0,int(min(a[1],b[1],c[1]))),min(W,int(max(a[1],b[1],c[1]))+1)):
    for x in range(max(0,int(min(a[0],b[0],c[0]))),min(W,int(max(a[0],b[0],c[0]))+1)):
     w1=((b[1]-c[1])*(x+.5-c[0])+(c[0]-b[0])*(y+.5-c[1]))/den;w2=((c[1]-a[1])*(x+.5-c[0])+(a[0]-c[0])*(y+.5-c[1]))/den;w3=1-w1-w2
     if min(w1,w2,w3)<-.0001:continue
     z=a[2]*w1+b[2]*w2+c[2]*w3;idx=y*W+x
     if z<=depth[idx]:continue
     depth[idx]=z;u=a[3]*w1+b[3]*w2+c[3]*w3;v=a[4]*w1+b[4]*w2+c[4]*w3
     rgb=tex[min(31,max(0,int(u*32))),min(31,max(0,int(v*32)))];pixels[x,y]=tuple(int(ch*shade) for ch in rgb)
preview=root/'build/previews';preview.mkdir(parents=True,exist_ok=True);canvas.save(preview/'block-model-preview.png')
atlas=Image.new('RGB',(6*128,128),(0,0,0))
for i,im in enumerate(textures):atlas.paste(im.resize((128,128),Image.Resampling.NEAREST),(i*128,0))
atlas.save(preview/'texture-atlas-preview.png')
print('Six 32x32 textures generated; preview cuboids:',len(parts))
