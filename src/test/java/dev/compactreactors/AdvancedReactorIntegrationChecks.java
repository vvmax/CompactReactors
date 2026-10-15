package dev.compactreactors;
import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
public final class AdvancedReactorIntegrationChecks {
 private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 public static void run(ExpansionModule first,ExpansionModule second) {
  cpw.mods.fml.common.registry.GameRegistry.registerTileEntity(AdvancedReactorTile.class,"compactreactors:advanced_reactor");
  check(new ReactorBlock(true).createNewTileEntity(null,0) instanceof AdvancedReactorTile,"advanced block creates ordinary tile");
  check(!((ReactorTile)new ReactorBlock().createNewTileEntity(null,0)).isAdvanced(),"ordinary block became advanced");
  AdvancedReactorTile tile=new AdvancedReactorTile();
  check(tile.isAdvanced() && tile.unlockedSize()==64 && tile.resize(6,8,7),"independent even dimensions need modules");
  check(!tile.isItemValidForSlot(PocketLayout.MODULE_1,new ItemStack(first)) && !tile.isItemValidForSlot(PocketLayout.MODULE_2,new ItemStack(second)),"advanced accepts expansion modules");
  for(int[] shape:new int[][]{{4,5,5},{5,65,5},{5,5,0}}) check(!tile.resize(shape[0],shape[1],shape[2]) && tile.resizeError==5,"axis range bypassed");
  tile.resize(5,5,5); tile.action(41); check(tile.dimensions().equals("6x5x5"),"X button changed another axis");
  tile.action(43); check(tile.dimensions().equals("6x6x5"),"Y button changed another axis");
  tile.action(45); check(tile.dimensions().equals("6x6x6"),"Z button changed another axis");
  tile.action(40); tile.action(42); tile.action(44); check(tile.dimensions().equals("5x5x5"),"minus buttons do not restore axes");
  tile.action(5); tile.action(6); check(tile.dimensions().equals("5x5x5"),"ordinary cube buttons changed advanced shape");
  BigReactors.maximumReactorHeight=7;
  check(tile.resize(9,7,9),"horizontal sides incorrectly limited by native height");
  check(!tile.resize(9,8,9) && tile.resizeError==4,"native Y limit ignored");
  BigReactors.maximumReactorHeight=9; BigReactors.maximumReactorSize=7;
  check(!tile.resize(8,5,7) && tile.resizeError==4,"native X limit ignored");
  check(!tile.resize(7,5,8) && tile.resizeError==4,"native Z limit ignored");
  BigReactors.maximumReactorSize=9;
  verifySaveAndClear(first,second);
  for(int x=5;x<=9;x++) for(int y=5;y<=9;y++) for(int z=5;z<=9;z++) verifyShape(x,y,z);
  verifyNetwork();
  System.out.println("PASS: advanced block type, all 125 rectangular shapes, independent axes without modules, native axis limits, liquid NBT and clear, cube save compatibility, complete-layer geometry and dimension sync");
 }
 private static void verifyShape(int width,int height,int depth) {
  AdvancedReactorTile tile=new AdvancedReactorTile(); check(tile.resize(width,height,depth),"rectangular resize rejected");
  ReactorContainer container=new ReactorContainer(new InventoryPlayer(null),tile);
  int volume=0,interior=0,controls=0;
  for(int yy=0;yy<10;yy++) for(int zz=0;zz<10;zz++) for(int xx=0;xx<10;xx++) { int i=PocketLayout.slot(xx,yy,zz);
   int x=PocketLayout.x(i),y=PocketLayout.y(i),z=PocketLayout.z(i);
   boolean active=x<width && y<height && z<depth;
   boolean shell=active && (x==0 || x==width-1 || y==0 || y==height-1 || z==0 || z==depth-1);
   boolean top=active && y==height-1 && x>0 && x<width-1 && z>0 && z<depth-1;
   check(tile.contains(i)==active && tile.boundary(i)==shell,"rectangular service footprint incorrect");
   check(PocketLayout.accepts(i,PocketLayout.CASING,width,height,depth)==shell,"rectangular casing acceptance");
   check(PocketLayout.accepts(i,PocketLayout.INTERIOR,width,height,depth)==(active && !shell),"rectangular interior acceptance");
   check(PocketLayout.accepts(i,PocketLayout.CONTROL,width,height,depth)==top,"control rod not on rectangular roof");
   if(active) volume++; if(active && !shell) interior++; if(top) controls++;
   int edges=(x==0 || x==width-1?1:0)+(y==0 || y==height-1?1:0)+(z==0 || z==depth-1?1:0);
   check(PocketLayout.accepts(i,PocketLayout.GLASS,width,height,depth)==(shell && edges==1),"glass allowed on edge or inside");
   check(PocketLayout.accepts(i,PocketLayout.SHELL_PART,width,height,depth)==(shell && edges==1),"port allowed on edge or inside");
  }
  check(volume==width*height*depth && interior==(width-2)*(height-2)*(depth-2) && controls==(width-2)*(depth-2),"rectangular volume");
  for(int layer=0;layer<height;layer++) {
   container.setLayer(layer); int visible=0;
   for(int page=0;page<ReactorContainer.PAGE_CELLS;page++) { int i=container.cellForPage(page);
    Slot slot=(Slot)container.inventorySlots.get(page); boolean shown=slot.yDisplayPosition>=0;
    check(shown==(tile.contains(i) && PocketLayout.y(i)==layer),"hidden or wrong rectangular layer cell");
    if(shown) { visible++; check(slot.yDisplayPosition>=56 && slot.yDisplayPosition+16<56+depth*18,"row outside expanded rectangular grid"); }
   }
   check(visible==width*depth,"entire rectangular layer not visible");
  }
  check(ReactorScreenLayout.height(tile)==254+(depth-5)*18,"GUI height follows Y instead of visible depth");
  for(Object object:container.inventorySlots) {
   Slot slot=(Slot)object; if(slot.yDisplayPosition<0) continue;
   check(slot.xDisplayPosition>=0 && slot.xDisplayPosition+16<318 && slot.yDisplayPosition+16<ReactorScreenLayout.height(tile),"rectangular slot outside window");
  }
  check(((Slot)container.inventorySlots.get(ReactorContainer.MODULE_1_SLOT)).yDisplayPosition<0 && ((Slot)container.inventorySlots.get(ReactorContainer.MODULE_2_SLOT)).yDisplayPosition<0,"advanced module slots visible");
  for(int[] resolution:new int[][]{{320,240},{340,260},{640,360}}) {
   float scale=ReactorScreenLayout.fitScale(resolution[0],resolution[1],tile);
   int w=ReactorScreenLayout.canvasExtent(resolution[0],scale),h=ReactorScreenLayout.canvasExtent(resolution[1],scale);
   check(w>=318 && h>=ReactorScreenLayout.height(tile),"advanced window does not fit screen");
  }
 }
 private static void verifySaveAndClear(ExpansionModule first,ExpansionModule second) {
  AdvancedReactorTile tile=new AdvancedReactorTile(); tile.resize(6,8,7);
  ItemStack liquid=new ItemStack(Items.water_bucket); liquid.setTagCompound(new NBTTagCompound()); liquid.getTagCompound().setString("source","advanced");
  int index=PocketLayout.slot(4,6,5); tile.setInventorySlotContents(index,liquid.copy());
  tile.energy=98765; tile.pocket=28; tile.deployed=true; tile.enabled=true;
  check(!tile.resize(7,8,7) && tile.resizeError==1,"advanced deployed resize accepted");
  NBTTagCompound saved=new NBTTagCompound(); tile.writeToNBT(saved);
  TileEntity loaded=TileEntity.createAndLoadEntity(saved);
  check(loaded instanceof AdvancedReactorTile,"advanced tile type lost during factory load");
  AdvancedReactorTile restored=(AdvancedReactorTile)loaded;
  check(restored.dimensions().equals("6x8x7") && restored.pocket==28 && restored.energy==98765 && restored.deployed && restored.enabled,"advanced dimensions or native state lost");
  check(ItemStack.areItemStacksEqual(restored.getStackInSlot(index),liquid),"advanced liquid NBT lost");
  restored.deployed=false;
  check(!restored.resize(7,8,7) && restored.resizeError==3 && restored.dimensions().equals("6x8x7"),"nonempty advanced resize accepted");
  InventoryPlayer inventory=new InventoryPlayer(null); ReactorContainer container=new ReactorContainer(inventory,restored);
  check(container.clearLayout() && !restored.hasLayout() && restored.dimensions().equals("6x8x7"),"rectangular clear changed dimensions");
  check(ItemStack.areItemStacksEqual(inventory.getStackInSlot(9),liquid),"rectangular clear lost liquid");
  check(restored.resize(5,9,8),"clear did not unlock axis changes");
  ReactorTile cube=new ReactorTile(); cube.setInventorySlotContents(PocketLayout.MODULE_1,new ItemStack(first)); cube.setInventorySlotContents(PocketLayout.MODULE_2,new ItemStack(second)); cube.resize(9);
  cube.setInventorySlotContents(index,liquid.copy()); cube.pocket=51;
  NBTTagCompound previous=new NBTTagCompound(); cube.writeToNBT(previous); previous.setInteger("LayoutVersion",3); net.minecraft.nbt.NBTTagList oldItems=previous.getTagList("Items",10); for(int j=0;j<oldItems.tagCount();j++) { NBTTagCompound entry=oldItems.getCompoundTagAt(j); int i=entry.getInteger("Slot"); int oldIndex=i>=PocketLayout.FUEL?729+i-PocketLayout.FUEL:PocketLayout.y(i)*81+PocketLayout.z(i)*9+PocketLayout.x(i); entry.setShort("Slot",(short)oldIndex); } previous.removeTag("SizeX"); previous.removeTag("SizeY"); previous.removeTag("SizeZ");
  ReactorTile old=new ReactorTile(); old.readFromNBT(previous);
  check(!old.isAdvanced() && old.size==9 && old.unlockedSize()==9 && old.pocket==51 && ItemStack.areItemStacksEqual(old.getStackInSlot(index),liquid),"0.2.x cube save changed");
  check(!old.resize(6,8,7),"ordinary block gained free rectangular dimensions");
 }
 private static void verifyNetwork() {
  AdvancedReactorTile client=new AdvancedReactorTile(); ReactorContainer container=new ReactorContainer(new InventoryPlayer(null),client);
  container.updateProgressBar(22,6); container.updateProgressBar(23,0);
  container.updateProgressBar(24,8); container.updateProgressBar(25,0);
  container.updateProgressBar(26,7); container.updateProgressBar(27,0); container.setLayer(7);
  check(client.dimensions().equals("6x8x7") && container.layer==7,"independent dimensions not synced");
  Slot corner=container.slotForCell(PocketLayout.slot(5,7,6));
  check(corner.yDisplayPosition==164,"last rectangular row missing after sync");
  client.action(44); container.refreshLayout();
  check(client.dimensions().equals("6x8x6") && corner.yDisplayPosition<0,"depth change did not update footprint");
 }
}
