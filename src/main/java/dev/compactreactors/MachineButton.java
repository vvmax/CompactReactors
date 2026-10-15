package dev.compactreactors;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;
@SideOnly(Side.CLIENT)
public final class MachineButton extends GuiButton {
 public boolean selected;
 public int accent=MachineTheme.GREEN;
 public MachineButton(int id,int x,int y,int w,int h,String text) { super(id,x,y,w,h,text); }
 @Override public void drawButton(Minecraft mc,int mx,int my) {
  if(!visible) return;
  boolean hover=mx>=xPosition && my>=yPosition && mx<xPosition+width && my<yPosition+height;
  GL11.glColor4f(1,1,1,1);
  drawRect(xPosition,yPosition,xPosition+width,yPosition+height,0xff111d16);
  drawRect(xPosition+1,yPosition+1,xPosition+width-1,yPosition+height-1,selected?0xff536742:enabled && hover?0xff536254:0xff34453a);
  drawRect(xPosition+1,yPosition+1,xPosition+width-1,yPosition+2,selected?0xffc4da8a:0xff7e8e77);
  drawRect(xPosition+1,yPosition+height-2,xPosition+width-1,yPosition+height-1,0xff243529);
  if(enabled || selected) drawRect(xPosition+2,yPosition+3,xPosition+3,yPosition+height-3,0xff000000|accent);
  String text=mc.fontRenderer.trimStringToWidth(displayString,Math.max(1,width-9));
  int color=selected?0xe0efbc:enabled?MachineTheme.TEXT:0x778879;
  mc.fontRenderer.drawString(text,xPosition+(width-mc.fontRenderer.getStringWidth(text))/2,yPosition+(height-8)/2,color);
  mouseDragged(mc,mx,my);
 }
}
