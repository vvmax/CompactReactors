package dev.compactreactors;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
@SideOnly(Side.CLIENT)
public final class StructureHelp extends Gui {
 private static final String[] ENTRIES={"casing","controller","access","power","control","glass","fuel"};
 private static final int PAGE_SIZE=3,PAGES=(ENTRIES.length+PAGE_SIZE-1)/PAGE_SIZE;
 private final Minecraft mc=Minecraft.getMinecraft();
 private final net.minecraft.client.renderer.entity.RenderItem itemRenderer=new net.minecraft.client.renderer.entity.RenderItem();
 private final int left,top;
 private final GuiButton back,previous,next;
 private int page=0;
 public StructureHelp(int left,int top) {
  this.left=left; this.top=top;
  back=new MachineButton(0,left+214,top+4,54,14,I18n.format("gui.compactreactors.catalog.back"));
  previous=new MachineButton(1,left+8,top+207,28,17,"<"); next=new MachineButton(2,left+240,top+207,28,17,">");
 }
 private String tr(String key) { return I18n.format("gui.compactreactors.structure."+key); }
 public void draw(int mx,int my) {
  drawRect(0,0,mc.currentScreen.width,mc.currentScreen.height,0xb0101820);
  MachineTheme.frame(left,top,276,234);
  mc.fontRenderer.drawString(tr("title"),left+8,top+8,MachineTheme.TEXT); back.drawButton(mc,mx,my);
  for(int row=0;row<PAGE_SIZE && page*PAGE_SIZE+row<ENTRIES.length;row++) {
   int index=page*PAGE_SIZE+row; String key=ENTRIES[index]; int y=top+27+row*49;
   MachineTheme.recess(left+6,y-2,264,47);
   icon(Parts.helpIcon(index),left+11,y+9);
   mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(tr(key+".name"),232),left+34,y,MachineTheme.GREEN);
   mc.fontRenderer.drawSplitString(tr(key+".body"),left+34,y+11,232,MachineTheme.TEXT);
  }
  mc.fontRenderer.drawSplitString(tr("note"),left+8,top+178,260,MachineTheme.MUTED);
  previous.enabled=page>0; next.enabled=page<PAGES-1; previous.drawButton(mc,mx,my); next.drawButton(mc,mx,my);
  mc.fontRenderer.drawString((page+1)+" / "+PAGES,left+120,top+212,MachineTheme.TEXT);
 }
 private void icon(net.minecraft.item.ItemStack stack,int x,int y) {
  if(stack==null) return;
  org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ENABLE_BIT|org.lwjgl.opengl.GL11.GL_CURRENT_BIT|org.lwjgl.opengl.GL11.GL_LIGHTING_BIT);
  org.lwjgl.opengl.GL11.glPushMatrix();
  try {
   org.lwjgl.opengl.GL11.glColor4f(1,1,1,1); org.lwjgl.opengl.GL11.glTranslatef(x,y,100);
   net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
   itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer,mc.getTextureManager(),stack,0,0);
  } finally { org.lwjgl.opengl.GL11.glPopMatrix(); org.lwjgl.opengl.GL11.glPopAttrib(); }
 }
 public boolean click(int x,int y,int button) {
  if(button!=0) return true;
  if(back.mousePressed(mc,x,y)) return false;
  if(previous.enabled && previous.mousePressed(mc,x,y)) page--;
  if(next.enabled && next.mousePressed(mc,x,y)) page++;
  return true;
 }
 public void wheel(int delta) { if(delta!=0) page=Math.max(0,Math.min(PAGES-1,page+(delta>0?-1:1))); }
}
