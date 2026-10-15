package dev.compactreactors;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import org.lwjgl.opengl.GL11;
@SideOnly(Side.CLIENT)
public class ReactorGui extends GuiContainer {
 private final ReactorContainer container;
 private final ReactorTile tile;
 private CoolantBrowser browser;
 private StructureHelp guide;
 private int screenWidth,screenHeight;
 private String displayedShape="";
 public ReactorGui(ReactorContainer c) { super(c); container=c; tile=c.tile; xSize=ReactorScreenLayout.width(tile); ySize=ReactorScreenLayout.height(tile); }
 private String tr(String s) { return I18n.format("gui.compactreactors."+s); }
 private int shift() { return ReactorScreenLayout.shift(tile); }
 private int extra() { return ReactorScreenLayout.extra(tile); }
 private CoolantBrowser createBrowser() { return new CoolantBrowser((width-276)/2,(height-234)/2,container.windowId); }
 @Override public void setWorldAndResolution(net.minecraft.client.Minecraft minecraft,int w,int h) {
  screenWidth=w; screenHeight=h;
  float scale=ReactorScreenLayout.fitScale(w,h,tile);
  super.setWorldAndResolution(minecraft,ReactorScreenLayout.canvasExtent(w,scale),ReactorScreenLayout.canvasExtent(h,scale));
 }
 @Override public void initGui() {
  xSize=ReactorScreenLayout.width(tile); ySize=ReactorScreenLayout.height(tile); displayedShape=tile.dimensions(); container.refreshLayout();
  super.initGui();
  if(browser!=null) browser=createBrowser();
  if(guide!=null) guide=new StructureHelp((width-276)/2,(height-234)/2);
  int shift=shift();
  buttonList.add(new MachineButton(21,guiLeft+216+shift,guiTop+3,18,12,"?"));
  buttonList.add(new MachineButton(20,guiLeft+236+shift,guiTop+3,74,12,tr("coolants")));
  buttonList.add(new MachineButton(30,guiLeft+8,guiTop+19,18,14,"<"));
  buttonList.add(new MachineButton(31,guiLeft+192+shift,guiTop+19,18,14,">"));
  if(tile.isAdvanced()) {
   for(int axis=0;axis<3;axis++) {
    int x=guiLeft+8+axis*104;
    buttonList.add(new MachineButton(40+axis*2,x+22,guiTop+38,18,14,"-"));
    buttonList.add(new MachineButton(41+axis*2,x+68,guiTop+38,18,14,"+"));
   }
  } else {
   buttonList.add(new MachineButton(5,guiLeft+224,guiTop+19,18,14,"-"));
   buttonList.add(new MachineButton(6,guiLeft+290,guiTop+19,18,14,"+"));
  }
  if(ReactorScreenLayout.paged(tile)) {
   buttonList.add(new MachineButton(50,guiLeft+8,guiTop+58,18,14,"<"));
   buttonList.add(new MachineButton(51,guiLeft+104,guiTop+58,18,14,">"));
   buttonList.add(new MachineButton(52,guiLeft+140,guiTop+58,18,14,"^"));
   buttonList.add(new MachineButton(53,guiLeft+246,guiTop+58,18,14,"v"));
  }
  int extra=extra();
  buttonList.add(new MachineButton(0,guiLeft+8,guiTop+130+extra,66,18,tr("power")));
  buttonList.add(new MachineButton(1,guiLeft+76,guiTop+130+extra,24,18,"-"));
  buttonList.add(new MachineButton(2,guiLeft+102,guiTop+130+extra,24,18,"+"));
  buttonList.add(new MachineButton(3,guiLeft+128,guiTop+130+extra,74+shift,18,tr("build")));
  buttonList.add(new MachineButton(4,guiLeft+204+shift,guiTop+130+extra,106,18,tr("edit")));
  buttonList.add(new MachineButton(7,guiLeft+8,guiTop+186+extra,64,18,tr("clear")));
 }
 @Override protected void actionPerformed(GuiButton b) {
  if(b.id==20) { browser=createBrowser(); return; }
  if(b.id==21) { guide=new StructureHelp((width-276)/2,(height-234)/2); return; }
  int id=b.id;
  if(id==30 || id==31) { container.setLayer(container.layer+(id==30?-1:1)); }
  if(id>=50 && id<=53) container.setView(container.column+(id==50?-10:id==51?10:0),container.row+(id==52?-10:id==53?10:0));
  mc.playerController.sendEnchantPacket(container.windowId,id);
 }
 @Override public void updateScreen() {
  super.updateScreen();
  if(!displayedShape.equals(tile.dimensions())) setWorldAndResolution(mc,screenWidth,screenHeight);
  if(browser!=null) browser.tick();
  for(Object o:buttonList) { GuiButton b=(GuiButton)o;
   if(b.id==0) { b.enabled=tile.deployed; b.displayString=tr(tile.enabled?"stop":"start"); }
   if(b.id==3) b.enabled=!tile.deployed;
   if(b.id==4) b.enabled=tile.deployed && !tile.enabled;
   if(b.id==5) b.enabled=!tile.deployed && tile.size>5;
   if(b.id==6) b.enabled=!tile.deployed && tile.size<tile.unlockedSize();
   if(b.id==7) { b.enabled=!tile.deployed && tile.hasLayout(); ((MachineButton)b).accent=MachineTheme.AMBER; }
   if(b.id==50) b.enabled=container.column>0;
   if(b.id==51) b.enabled=container.column<tile.sizeX()-10;
   if(b.id==52) b.enabled=container.row>0;
   if(b.id==53) b.enabled=container.row<tile.sizeZ()-10;
   if(b.id==30) b.enabled=container.layer>0;
   if(b.id==31) b.enabled=container.layer<tile.sizeY()-1;
   if(b.id>=40 && b.id<=45) {
    int axis=(b.id-40)/2,value=axis==0?tile.sizeX():axis==1?tile.sizeY():tile.sizeZ();
    b.enabled=!tile.deployed && (b.id%2==0?value>5:value<(axis==0?tile.limitX:axis==1?tile.limitY:tile.limitZ));
   }
   if(b.id==0) ((MachineButton)b).accent=tile.enabled?MachineTheme.AMBER:MachineTheme.GREEN;
  }
 }
 @Override public void drawScreen(int mx,int my,float partial) {
  int localX=mx*width/Math.max(1,screenWidth),localY=my*height/Math.max(1,screenHeight);
  GL11.glPushMatrix();
  GL11.glScalef(screenWidth/(float)width,screenHeight/(float)height,1);
  try { drawContents(localX,localY,partial); } finally { GL11.glPopMatrix(); }
 }
 private void drawContents(int mx,int my,float partial) {
  if(guide!=null) { guide.draw(mx,my); return; }
  if(browser!=null) { browser.draw(mx,my); return; }
  super.drawScreen(mx,my,partial);
  int extra=extra(),header=ReactorScreenLayout.header(tile),shift=shift();
  if(mx>=guiLeft+216+shift && mx<guiLeft+234+shift && my>=guiTop+3 && my<guiTop+15)
   drawHoveringText(java.util.Arrays.asList(tr("structure.tip")),mx,my,fontRendererObj);
  if(mx>=guiLeft+8 && mx<guiLeft+72 && my>=guiTop+186+extra && my<guiTop+204+extra)
   drawHoveringText(java.util.Arrays.asList(tr("clear.tip1"),tr("clear.tip2"),tr("clear.tip3")),mx,my,fontRendererObj);
  if(mx>=guiLeft+204+shift && mx<guiLeft+310+shift && my>=guiTop+130+extra && my<guiTop+148+extra)
   drawHoveringText(java.util.Arrays.asList(tr("edit.tip1"),tr("edit.tip2")),mx,my,fontRendererObj);
  if(mx>=guiLeft+182+shift && mx<guiLeft+308+shift && my>=guiTop+120+extra && my<guiTop+126+extra)
   drawHoveringText(java.util.Arrays.asList(tile.energy+" / "+ReactorTile.CAPACITY+" RF"),mx,my,fontRendererObj);
  if(mx>=guiLeft+206+shift && mx<guiLeft+242+shift && my>=guiTop+100+header && my<guiTop+117+header)
   drawHoveringText(java.util.Arrays.asList(tr("fuel")+": "+tile.fuel+" mB"),mx,my,fontRendererObj);
  if(mx>=guiLeft+270+shift && mx<guiLeft+308+shift && my>=guiTop+100+header && my<guiTop+117+header)
   drawHoveringText(java.util.Arrays.asList(tr("waste")+": "+tile.waste+" mB"),mx,my,fontRendererObj);
  if(mx>=guiLeft+224+shift && mx<guiLeft+308+shift && my>=guiTop+19 && my<guiTop+33) {
   java.util.List<String> lines=new java.util.ArrayList<String>(); lines.add(tr(tile.isAdvanced()?"advanced.axes":"size.tip"));
   lines.add(tr("size.empty")); if(tile.resizeError!=0) lines.add("\u00a76"+tr("resize.error."+tile.resizeError));
   drawHoveringText(lines,mx,my,fontRendererObj);
  }
  if(!tile.isAdvanced() && mx>=guiLeft+250+shift && mx<guiLeft+310+shift && my>=guiTop+192+extra && my<guiTop+224+extra)
   drawHoveringText(java.util.Arrays.asList(tr("modules.tip1"),tr("modules.tip2")),mx,my,fontRendererObj);
  if(mx>=guiLeft+252+shift && mx<guiLeft+308+shift && my>=guiTop+76+header && my<guiTop+88+header)
   drawHoveringText(java.util.Arrays.asList(tr("rods")+" "+tile.insertion+"%"),mx,my,fontRendererObj);
  if(tile.isAdvanced() && my>=guiTop+36 && my<guiTop+54) {
   int axis=(mx-guiLeft-8)/104;
   if(mx>=guiLeft+8 && mx<guiLeft+310+shift && axis>=0 && axis<3) {
    java.util.List<String> lines=new java.util.ArrayList<String>();
    lines.add(tr("axis."+axis)); lines.add(tr("advanced.range")+" X:"+tile.limitX+" Y:"+tile.limitY+" Z:"+tile.limitZ); lines.add(tr("size.empty"));
    if(tile.resizeError!=0) lines.add("\u00a76"+tr("resize.error."+tile.resizeError));
    drawHoveringText(lines,mx,my,fontRendererObj);
   }
  }
 }
 @Override protected void renderToolTip(net.minecraft.item.ItemStack stack,int x,int y) {
  if(LiquidCoolants.bulkContainer(stack)) {
   java.util.List<String> lines=new java.util.ArrayList<String>(stack.getTooltip(mc.thePlayer,mc.gameSettings.advancedItemTooltips));
   lines.addAll(fontRendererObj.listFormattedStringToWidth("\u00a7b"+tr("liquid.bulk.click"),Math.min(280,width-24)));
   lines.addAll(fontRendererObj.listFormattedStringToWidth("\u00a77"+tr("liquid.bulk.shift"),Math.min(280,width-24)));
   drawHoveringText(lines,x,y,fontRendererObj); return;
  }
  net.minecraftforge.fluids.FluidStack fluid=LiquidCoolants.fluidForCell(stack);
  if(fluid==null) { super.renderToolTip(stack,x,y); return; }
  java.util.List<String> lines=new java.util.ArrayList<String>(stack.getTooltip(mc.thePlayer,mc.gameSettings.advancedItemTooltips));
  lines.add("\u00a7b"+fluid.getLocalizedName()+" - 1000 mB");
  lines.add("\u00a77"+tr(stack.getItem() instanceof CoolantCell?"liquid.portion":"liquid.held"));
  drawHoveringText(lines,x,y,fontRendererObj);
 }
 @Override protected void mouseClicked(int x,int y,int button) {
  if(guide!=null) { if(!guide.click(x,y,button)) guide=null; return; }
  if(browser!=null) { if(!browser.click(x,y,button)) browser=null; return; }
  super.mouseClicked(x,y,button);
 }
 @Override protected void mouseMovedOrUp(int x,int y,int button) { if(browser==null && guide==null) super.mouseMovedOrUp(x,y,button); }
 @Override protected void keyTyped(char c,int key) {
  if(guide!=null) { if(key==org.lwjgl.input.Keyboard.KEY_ESCAPE) guide=null; return; }
  if(browser!=null) {
   if(key==org.lwjgl.input.Keyboard.KEY_ESCAPE) { if(!browser.back()) browser=null; }
   else browser.key(c,key);
   return;
  }
  super.keyTyped(c,key);
 }
 @Override public void handleMouseInput() {
  super.handleMouseInput();
  int wheel=org.lwjgl.input.Mouse.getEventDWheel();
  if(guide!=null) guide.wheel(wheel); else if(browser!=null) browser.wheel(wheel);
 }
 @Override protected void drawGuiContainerBackgroundLayer(float f,int mx,int my) {
  GL11.glColor4f(1,1,1,1);
  int extra=extra(),header=ReactorScreenLayout.header(tile),rows=ReactorScreenLayout.extra(Math.min(10,tile.sizeZ())),shift=shift();
  MachineTheme.frame(guiLeft,guiTop,xSize,ySize);
  MachineTheme.recess(guiLeft+8,guiTop+35+header,168+shift,93+rows);
  MachineTheme.recess(guiLeft+178+shift,guiTop+36+header,132,91+rows);
  MachineTheme.recess(guiLeft+74+shift/2,guiTop+150+extra,170,78);
  MachineTheme.recess(guiLeft+250+shift,guiTop+150+extra,60,78);
  MachineTheme.vent(guiLeft+13,guiTop+207+extra,49,1);
  MachineTheme.hazard(guiLeft+13,guiTop+213+extra,49,6);
  if(rows>0) MachineTheme.vent(guiLeft+188+shift,guiTop+128+header,112,(rows-12)/5);
  if(tile.isAdvanced()) MachineTheme.vent(guiLeft+258+shift,guiTop+171+extra,44,10);
  for(Object o:container.inventorySlots) { Slot slot=(Slot)o; if(slot.yDisplayPosition<0) continue;
   MachineTheme.slot(guiLeft+slot.xDisplayPosition,guiTop+slot.yDisplayPosition,slot.slotNumber<ReactorContainer.PAGE_CELLS && tile.boundary(slot.getSlotIndex()));
  }
  MachineTheme.bar(guiLeft+182+shift,guiTop+120+extra,126,5,(float)tile.energy/ReactorTile.CAPACITY,MachineTheme.GREEN);
 }
 private String compact(int amount) { return amount>=1000000?String.format(java.util.Locale.ROOT,"%.1fM",amount/1000000f):amount>=10000?String.format(java.util.Locale.ROOT,"%.0fk",amount/1000f):Integer.toString(amount); }
 @Override protected void drawGuiContainerForegroundLayer(int mx,int my) {
  fontRendererObj.drawString(fontRendererObj.trimStringToWidth(I18n.format(tile.getInventoryName()),199+shift()),11,6,MachineTheme.TEXT);
  String layer=tr(container.layer==0?"bottom":container.layer==tile.sizeY()-1?"roof":"layer")+(container.layer==0 || container.layer==tile.sizeY()-1?"":" "+container.layer);
  layer+=" ("+(container.layer+1)+"/"+tile.sizeY()+")";
  fontRendererObj.drawString(layer,110+shift()/2-fontRendererObj.getStringWidth(layer)/2,23,MachineTheme.TEXT);
  fontRendererObj.drawString(tile.dimensions(),248+shift(),23,tile.resizeError==0?MachineTheme.GREEN:MachineTheme.AMBER);
  int extra=extra(),header=ReactorScreenLayout.header(tile),shift=shift();
  if(ReactorScreenLayout.paged(tile)) {
   fontRendererObj.drawString("X "+(container.column+1)+"-"+Math.min(container.column+10,tile.sizeX())+"/"+tile.sizeX(),30,62,MachineTheme.MUTED);
   fontRendererObj.drawString("Z "+(container.row+1)+"-"+Math.min(container.row+10,tile.sizeZ())+"/"+tile.sizeZ(),166,62,MachineTheme.MUTED);
  }
  fontRendererObj.drawString(tr(tile.isAdvanced()?"advanced.free":"modules"),254+shift,155+extra,MachineTheme.MUTED);
  if(!tile.isAdvanced()) { fontRendererObj.drawString("I",265+shift,193+extra,MachineTheme.GREEN); fontRendererObj.drawString("II",287+shift,193+extra,MachineTheme.AMBER); }
  else for(int axis=0;axis<3;axis++) {
   int x=8+axis*104,value=axis==0?tile.sizeX():axis==1?tile.sizeY():tile.sizeZ();
   fontRendererObj.drawString(axis==0?"X":axis==1?"Y":"Z",x+4,42,MachineTheme.BLUE);
   fontRendererObj.drawString(Integer.toString(value),x+50,42,MachineTheme.TEXT);
  }
  fontRendererObj.drawString(tr("size"),13,155+extra,MachineTheme.MUTED);
  fontRendererObj.drawString(tile.dimensions(),13,168+extra,MachineTheme.GREEN);
  int status=tile.status==3 || tile.status==2?MachineTheme.GREEN:tile.status==1 || tile.status==4?MachineTheme.AMBER:MachineTheme.MUTED;
  drawRect(182+shift,40+header,185+shift,47+header,0xff000000|status);
  fontRendererObj.drawString(fontRendererObj.trimStringToWidth(tr("status."+tile.status),117),189+shift,39+header,status);
  fontRendererObj.drawString(tile.production+" RF/t",182+shift,53+header,MachineTheme.GREEN);
  fontRendererObj.drawString(compact(tile.energy)+" / 1M RF",182+shift,65+header,MachineTheme.TEXT);
  fontRendererObj.drawString(tile.heat+" C",182+shift,78+header,MachineTheme.AMBER);
  fontRendererObj.drawString(tile.insertion+"%",272+shift,78+header,MachineTheme.MUTED);
  fontRendererObj.drawString(tr("fuel"),182+shift,91+header,MachineTheme.MUTED);
  fontRendererObj.drawString(tr("waste"),248+shift,91+header,MachineTheme.MUTED);
  fontRendererObj.drawString(compact(tile.fuel),206+shift,104+header,MachineTheme.TEXT);
  fontRendererObj.drawString(compact(tile.waste),270+shift,104+header,MachineTheme.TEXT);
 }
}
