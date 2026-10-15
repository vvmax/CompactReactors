package dev.compactreactors;
import java.util.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.*;
@SideOnly(Side.CLIENT)
public final class CoolantBrowser extends Gui {
 private final Minecraft mc=Minecraft.getMinecraft();
 private final FontRenderer font=mc.fontRenderer;
 private final net.minecraft.client.renderer.entity.RenderItem itemRenderer=new net.minecraft.client.renderer.entity.RenderItem();
 private final int left,top,window;
 private final GuiTextField search;
 private final GuiButton back,filter,previous,next;
 private int page=0,type=0,column=0;
 private boolean descending=false;
 private CoolantEntry selected;
 private static final int PAGE_SIZE=10;
 public CoolantBrowser(int left,int top,int window) {
  this.left=left; this.top=top; this.window=window;
  search=new GuiTextField(font,left+12,top+30,146,12); search.setMaxStringLength(80); search.setEnableBackgroundDrawing(false); search.setTextColor(MachineTheme.TEXT);
  back=new MachineButton(0,left+214,top+4,54,16,tr("back"));
  filter=new MachineButton(1,left+170,top+25,98,18,tr("filter.0"));
  previous=new MachineButton(2,left+8,top+199,28,17,"<");
  next=new MachineButton(3,left+240,top+199,28,17,">");
 }
 private String tr(String key) { return I18n.format("gui.compactreactors.catalog."+key); }
 private String name(CoolantEntry e) {
  if(e.type!=CoolantEntry.FLUID && e.icon!=null) return e.icon.getDisplayName();
  if(e.type==CoolantEntry.FLUID) {
   Fluid fluid=FluidRegistry.getFluid(e.key);
   if(fluid!=null) return fluid.getLocalizedName(new FluidStack(fluid,1000));
  }
  String key="gui.compactreactors.catalog.material."+e.key;
  String translated=I18n.format(key); return key.equals(translated)?e.key:translated;
 }
 private String availability(CoolantEntry e) { return tr(!e.available?"unavailable":e.usable?(e.type==CoolantEntry.FLUID?"fluid.usable":"usable"):"reference"); }
 private List<CoolantEntry> rows() {
  CatalogNetwork.State state=CatalogNetwork.current(window);
  List<CoolantEntry> out=new ArrayList<CoolantEntry>();
  if(state!=null) for(CoolantEntry e:state.entries) {
   boolean matchType=type==0 || (type==1 && e.type!=CoolantEntry.FLUID) || (type==2 && e.type==CoolantEntry.FLUID);
   String query=search.getText().trim().toLowerCase(Locale.ROOT);
   if(e.available && matchType && (e.key.toLowerCase(Locale.ROOT).contains(query) || name(e).toLowerCase(Locale.ROOT).contains(query))) out.add(e);
  }
  Collections.sort(out,new Comparator<CoolantEntry>() {
   @Override public int compare(CoolantEntry a,CoolantEntry b) {
    int compared=column==0?name(a).compareToIgnoreCase(name(b)):Float.compare(a.metric(column),b.metric(column));
    if(compared==0) compared=a.key.compareTo(b.key);
    return descending?-compared:compared;
   }
  });
  page=Math.max(0,Math.min(page,Math.max(0,(out.size()-1)/PAGE_SIZE)));
  return out;
 }
 private String value(CoolantEntry e,int column,boolean detailed) {
  float n=e.metric(column);
  if(Float.isNaN(n) || Float.isInfinite(n)) return "?";
  if(column<=2) return String.format(Locale.ROOT,detailed?"%.1f%%":"%.0f%%",n*100);
  return String.format(Locale.ROOT,detailed?"%.3f":"%.2f",n)+(column==3?"x":"");
 }
 public void draw(int mx,int my) {
  drawRect(0,0,mc.currentScreen.width,mc.currentScreen.height,0xb0101820);
  MachineTheme.frame(left,top,276,234);
  font.drawString(tr("title"),left+8,top+8,MachineTheme.TEXT);
  back.drawButton(mc,mx,my);
  if(selected!=null) { detail(); return; }
  MachineTheme.recess(left+8,top+25,154,19);
  search.drawTextBox();
  if(search.getText().isEmpty() && !search.isFocused()) font.drawString(tr("search"),left+12,top+30,MachineTheme.MUTED);
  filter.displayString=tr("filter."+type); filter.drawButton(mc,mx,my);
  font.drawString(tr("name")+(column==0?(descending?" -":" +"):""),left+8,top+49,MachineTheme.TEXT);
  for(int i=1;i<=4;i++) font.drawString(tr("short."+i),left+122+(i-1)*36,top+49,column==i?MachineTheme.GREEN:MachineTheme.TEXT);
  List<CoolantEntry> all=rows();
  for(int row=0;row<PAGE_SIZE && page*PAGE_SIZE+row<all.size();row++) {
   CoolantEntry e=all.get(page*PAGE_SIZE+row); int y=top+62+row*13;
   boolean hover=mx>=left+6 && mx<left+270 && my>=y-1 && my<y+12;
   drawRect(left+6,y-1,left+270,y+12,hover?0xff445c43:row%2==0?0xff1d3024:0xff263b2b);
   int color=e.usable?MachineTheme.TEXT:MachineTheme.BLUE;
   drawRect(left+7,y,left+9,y+9,e.type==CoolantEntry.FLUID?0xff3f82ad:0xff43805c);
   icon(e,left+12,y,11);
   font.drawString(font.trimStringToWidth(name(e),91),left+27,y+1,color);
   for(int i=1;i<=4;i++) font.drawString(value(e,i,false),left+122+(i-1)*36,y+1,MachineTheme.TEXT);
  }
  if(all.isEmpty()) font.drawSplitString(tr(CatalogNetwork.current(window)==null?"loading":"empty"),left+8,top+68,256,MachineTheme.TEXT);
  previous.enabled=page>0; next.enabled=(page+1)*PAGE_SIZE<all.size();
  previous.drawButton(mc,mx,my); next.drawButton(mc,mx,my);
  String pages=(page+1)+" / "+Math.max(1,(all.size()+PAGE_SIZE-1)/PAGE_SIZE)+"  ("+all.size()+")";
  font.drawString(pages,left+(276-font.getStringWidth(pages))/2,top+204,MachineTheme.TEXT);
  String hint=tr("hint");
  if(my>=top+46 && my<top+60 && mx>=left+122 && mx<left+268) hint=tr("label."+(1+Math.min(3,(mx-left-122)/36)))+": "+tr("sort");
  font.drawSplitString(hint,left+8,top+219,260,MachineTheme.MUTED);
 }
 private void detail() {
  icon(selected,left+8,top+26,24);
  font.drawString(font.trimStringToWidth(name(selected),230),left+38,top+26,MachineTheme.TEXT);
  font.drawString(font.trimStringToWidth(selected.key,230),left+38,top+37,MachineTheme.MUTED);
  font.drawString(font.trimStringToWidth(availability(selected),230),left+38,top+48,selected.usable?MachineTheme.GREEN:MachineTheme.MUTED);
  for(int i=1;i<=4;i++) {
   int y=top+65+(i-1)*30;
   MachineTheme.recess(left+6,y-2,264,29);
   font.drawString(tr("label."+i)+": "+value(selected,i,true),left+10,y,MachineTheme.GREEN);
   font.drawSplitString(tr("effect."+i),left+8,y+10,260,MachineTheme.MUTED);
  }
  font.drawSplitString(tr("context"),left+8,top+191,260,MachineTheme.MUTED);
 }
 private void icon(CoolantEntry entry,int x,int y,int size) {
  org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ENABLE_BIT|org.lwjgl.opengl.GL11.GL_CURRENT_BIT|org.lwjgl.opengl.GL11.GL_LIGHTING_BIT);
  org.lwjgl.opengl.GL11.glPushMatrix();
  try {
   org.lwjgl.opengl.GL11.glColor4f(1,1,1,1);
   if(entry.icon!=null) {
    org.lwjgl.opengl.GL11.glTranslatef(x,y,100); org.lwjgl.opengl.GL11.glScalef(size/16f,size/16f,1);
    net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
    itemRenderer.renderItemAndEffectIntoGUI(font,mc.getTextureManager(),entry.icon,0,0);
   } else {
    net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
    Fluid fluid=entry.type==CoolantEntry.FLUID?FluidRegistry.getFluid(entry.key):null;
    if(fluid!=null && fluid.getStillIcon()!=null) {
     int color=fluid.getColor(); org.lwjgl.opengl.GL11.glColor4f(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f,1);
     mc.getTextureManager().bindTexture(net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture);
     drawTexturedModelRectFromIcon(x,y,fluid.getStillIcon(),size,size);
    } else {
     int color=entry.type==CoolantEntry.AIR?0xffa9c9c6:0xff83b8bb;
     drawRect(x+1,y+size/3,x+size-1,y+size/3+1,color);
     drawRect(x+size/3,y+size/2,x+size,y+size/2+1,color);
     drawRect(x,y+size*2/3,x+size*2/3,y+size*2/3+1,color);
    }
   }
  } finally { org.lwjgl.opengl.GL11.glPopMatrix(); org.lwjgl.opengl.GL11.glPopAttrib(); }
 }
 /** false means return to the editor, without closing its server container. */
 public boolean back() { if(selected!=null) { selected=null; return true; } return false; }
 public boolean click(int mx,int my,int button) {
  if(button!=0) return true;
  if(back.mousePressed(mc,mx,my)) { back.func_146113_a(mc.getSoundHandler()); return back(); }
  if(selected!=null) return true;
  search.mouseClicked(mx,my,button);
  if(filter.mousePressed(mc,mx,my)) { type=(type+1)%3; page=0; return true; }
  List<CoolantEntry> all=rows();
  if(page>0 && previous.mousePressed(mc,mx,my)) { page--; return true; }
  if((page+1)*PAGE_SIZE<all.size() && next.mousePressed(mc,mx,my)) { page++; return true; }
  if(my>=top+46 && my<top+60 && mx>=left+8 && mx<left+268) {
   int selectedColumn=mx<left+122?0:1+Math.min(3,(mx-left-122)/36);
   if(column==selectedColumn) descending=!descending; else { column=selectedColumn; descending=column!=0; }
   page=0; return true;
  }
  int row=(my-top-62)/13;
  if(mx>=left+6 && mx<left+270 && my>=top+62 && row>=0 && row<PAGE_SIZE && page*PAGE_SIZE+row<all.size()) selected=all.get(page*PAGE_SIZE+row);
  return true;
 }
 public void key(char c,int code) { if(selected==null && search.textboxKeyTyped(c,code)) page=0; }
 public void tick() { search.updateCursorCounter(); }
 public void wheel(int delta) {
  if(selected==null && delta!=0) { List<CoolantEntry> all=rows(); page=Math.max(0,Math.min(Math.max(0,(all.size()-1)/PAGE_SIZE),page+(delta<0?1:-1))); }
 }
}
