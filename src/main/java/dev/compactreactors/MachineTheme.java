package dev.compactreactors;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.Gui;
@SideOnly(Side.CLIENT)
public final class MachineTheme extends Gui {
 public static final int TEXT=0xdce4d3,MUTED=0x9aada0,GREEN=0xb5d76b,AMBER=0xe3bd62,BLUE=0x83b8bb;
 private MachineTheme() {}
 public static void frame(int x,int y,int w,int h) {
  drawRect(x,y,x+w,y+h,0xff101715);
  drawRect(x+1,y+1,x+w-1,y+h-1,0xff929b91);
  drawRect(x+2,y+2,x+w-2,y+h-2,0xff48554e);
  drawRect(x+3,y+3,x+w-3,y+h-3,0xff59665b);
  drawRect(x+3,y+3,x+w-3,y+18,0xff202c25);
  drawRect(x+3,y+18,x+w-3,y+19,0xffa9ba87);
  drawRect(x+4,y+h-5,x+w-4,y+h-3,0xff354339);
  bolt(x+3,y+3); bolt(x+w-7,y+3); bolt(x+3,y+h-7); bolt(x+w-7,y+h-7);
 }
 private static void bolt(int x,int y) {
  drawRect(x,y,x+4,y+4,0xff1c2921); drawRect(x+1,y+1,x+3,y+3,0xffa8b3a0); drawRect(x+1,y+2,x+3,y+3,0xff66735f);
 }
 public static void recess(int x,int y,int w,int h) {
  drawRect(x,y,x+w,y+h,0xff25342b); drawRect(x+1,y+1,x+w-1,y+h-1,0xff121e17);
  drawRect(x+1,y+h-1,x+w,y+h,0xff7f8c77);
 }
 public static void slot(int x,int y,boolean boundary) {
  drawRect(x-1,y-1,x+17,y+17,0xff111d15);
  drawRect(x,y,x+16,y+16,boundary?0xff61705d:0xff34483a);
  drawRect(x+1,y+1,x+15,y+15,boundary?0xff4b5c48:0xff26392c);
  drawRect(x+2,y+14,x+15,y+15,0xff71816a);
 }
 public static void bar(int x,int y,int w,int h,float ratio,int color) {
  recess(x,y,w,h);
  int fill=(int)((w-2)*Math.max(0,Math.min(1,ratio)));
  if(fill>0) { drawRect(x+1,y+1,x+1+fill,y+h-1,0xff000000|color); drawRect(x+1,y+1,x+1+fill,y+2,0xffd4e4a0); }
 }
 public static void vent(int x,int y,int w,int count) {
  for(int i=0;i<count;i++) { drawRect(x,y+i*5,x+w,y+i*5+2,0xff233126); drawRect(x+1,y+i*5+2,x+w,y+i*5+3,0xff7b8870); }
 }
 public static void hazard(int x,int y,int w,int h) {
  drawRect(x,y,x+w,y+h,0xff263127);
  for(int row=0;row<h;row++) for(int col=0;col<w;col++) if(((col+row)/4)%2==0) drawRect(x+col,y+row,x+col+1,y+row+1,0xffc5aa58);
 }
}
