package dev.compactreactors;
import erogenousbeef.bigreactors.common.BigReactors;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
/** Runs inside the existing Forge headless harness, after vanilla/fluid registration. */
public final class ExpansionIntegrationChecks {
 private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 public static void run() throws Exception {
  java.lang.reflect.Field mods=cpw.mods.fml.common.Loader.class.getDeclaredField("namedMods");
  mods.setAccessible(true); mods.set(cpw.mods.fml.common.Loader.instance(),new java.util.HashMap<String,cpw.mods.fml.common.ModContainer>());
  java.lang.reflect.Field apis=cpw.mods.fml.common.ModAPIManager.class.getDeclaredField("apiContainers");
  apis.setAccessible(true); apis.set(cpw.mods.fml.common.ModAPIManager.INSTANCE,new java.util.HashMap<String,Object>());
  int previousWidth=BigReactors.maximumReactorSize,previousHeight=BigReactors.maximumReactorHeight;
  BigReactors.maximumReactorSize=9; BigReactors.maximumReactorHeight=9;
  ExpansionModule first=new ExpansionModule(1),second=new ExpansionModule(2);
  Item.itemRegistry.addObject(31001,"compactreactors:test_expansion1",first);
  Item.itemRegistry.addObject(31002,"compactreactors:test_expansion2",second);
  ReactorTile tile=new ReactorTile();
  check(!tile.resize(7) && tile.size==5 && tile.resizeError==2,"size bypassed modules");
  check(!tile.isItemValidForSlot(PocketLayout.MODULE_2,new ItemStack(second)),"tier II without tier I");
  InventoryPlayer inventory=new InventoryPlayer(null); ReactorContainer container=new ReactorContainer(inventory,tile);
  inventory.setInventorySlotContents(9,new ItemStack(first));
  check(container.transferStackInSlot(null,ReactorContainer.PLAYER_START)!=null && inventory.getStackInSlot(9)==null,"module shift insertion");
  check(tile.unlockedSize()==7 && tile.resize(7),"tier I did not unlock seven");
  check(!((Slot)container.inventorySlots.get(ReactorContainer.MODULE_1_SLOT)).canTakeStack(null),"active capacity module removable");
  check(container.transferStackInSlot(null,ReactorContainer.MODULE_1_SLOT)==null,"shift removed required module");
  check(!tile.isItemValidForSlot(PocketLayout.MODULE_2,new ItemStack(first)),"wrong module tier accepted");
  inventory.setInventorySlotContents(9,new ItemStack(second));
  check(container.transferStackInSlot(null,ReactorContainer.PLAYER_START)!=null && tile.resize(9),"tier II did not unlock nine");
  check(tile.unlockedSize()==9 && !tile.canRemoveModule(PocketLayout.MODULE_2),"module protection at nine");
  tile.deployed=true; check(!tile.resize(5) && tile.resizeError==1,"deployed resize accepted");
  check(!tile.canRemoveModule(PocketLayout.MODULE_1) && !tile.isItemValidForSlot(PocketLayout.MODULE_2,new ItemStack(second)),"deployed module editing accepted");
  tile.deployed=false;
  int distant=PocketLayout.slot(7,7,7); ItemStack coolant=new ItemStack(Items.water_bucket);
  coolant.setTagCompound(new NBTTagCompound()); coolant.getTagCompound().setString("OwnerTag","preserve me");
  check(distant>255 && tile.isItemValidForSlot(distant,coolant),"large liquid cell unavailable");
  tile.setInventorySlotContents(distant,coolant.copy());
  check(!tile.resize(7) && tile.resizeError==3 && ItemStack.areItemStacksEqual(tile.getStackInSlot(distant),coolant),"nonempty resize lost material");
  tile.energy=123456; tile.pocket=42; tile.insertion=30; tile.deployed=true; tile.enabled=true;
  NBTTagCompound saved=new NBTTagCompound(); tile.writeToNBT(saved);
  ReactorTile restored=new ReactorTile(); restored.readFromNBT(saved);
  check(restored.size==9 && restored.unlockedSize()==9 && restored.deployed && restored.enabled && restored.pocket==42 && restored.energy==123456,"expanded state lost on reload");
  check(ItemStack.areItemStacksEqual(restored.getStackInSlot(distant),coolant),"short slot index or NBT lost");
  check(ExpansionModule.matches(restored.getStackInSlot(PocketLayout.MODULE_1),1) && ExpansionModule.matches(restored.getStackInSlot(PocketLayout.MODULE_2),2),"saved modules lost");
  restored.deployed=false; check(restored.decrStackSize(distant,1)!=null,"large cell return failed");
  check(restored.resize(7) && restored.canRemoveModule(PocketLayout.MODULE_2),"tier II remains locked after shrinking");
  restored.decrStackSize(PocketLayout.MODULE_2,1);
  check(restored.resize(5) && restored.canRemoveModule(PocketLayout.MODULE_1),"tier I remains locked after shrinking");
  restored.decrStackSize(PocketLayout.MODULE_1,1); check(restored.unlockedSize()==5,"module removal did not reduce capacity");
  BigReactors.maximumReactorSize=7; check(!tile.resize(9),"native size limit bypassed");
  tile.deployed=false; tile.setInventorySlotContents(distant,null);
  check(!tile.resize(9) && tile.resizeError==4,"native width limit ignored");
  BigReactors.maximumReactorSize=9; BigReactors.maximumReactorHeight=7;
  check(!tile.resize(9) && tile.resizeError==4,"native height limit ignored");
  BigReactors.maximumReactorHeight=9;
  verifyMigration(); verifyViewport(first,second);
  ClearLayoutIntegrationChecks.run(first,second);
  AdvancedReactorIntegrationChecks.run(first,second);
  LargeReactorIntegrationChecks.run();
  BulkFluidIntegrationChecks.run();
  BigReactors.maximumReactorSize=previousWidth; BigReactors.maximumReactorHeight=previousHeight;
  System.out.println("PASS: tiered expansion, shift transfers and module locks, native size limits, 9x9x9 short-slot NBT, old-save migration and full editor reachability");
 }
 private static void verifyMigration() {
  NBTTagCompound old=new NBTTagCompound(); old.setInteger("LayoutVersion",2); old.setInteger("Pocket",19);
  old.setBoolean("Deployed",true); old.setBoolean("Enabled",true); old.setInteger("Energy",76543);
  NBTTagList list=new NBTTagList();
  int[] indices={62,125,126}; ItemStack[] stacks={new ItemStack(Items.water_bucket),new ItemStack(Items.redstone,17),new ItemStack(Items.gold_ingot,11)};
  for(int i=0;i<indices.length;i++) { NBTTagCompound entry=new NBTTagCompound(); stacks[i].writeToNBT(entry); entry.setByte("Slot",(byte)indices[i]); list.appendTag(entry); }
  old.setTag("Items",list); ReactorTile migrated=new ReactorTile(); migrated.readFromNBT(old);
  check(migrated.size==5 && migrated.pocket==19 && migrated.deployed && migrated.enabled && migrated.energy==76543,"old native reactor identity changed");
  for(int i=0;i<indices.length;i++) check(ItemStack.areItemStacksEqual(migrated.getStackInSlot(PocketLayout.versionTwoSlot(indices[i])),stacks[i]),"old save item lost");
 }
 private static void verifyViewport(ExpansionModule first,ExpansionModule second) {
  for(int size:new int[]{5,7,9}) {
   ReactorTile tile=new ReactorTile(); tile.setInventorySlotContents(PocketLayout.MODULE_1,new ItemStack(first)); tile.setInventorySlotContents(PocketLayout.MODULE_2,new ItemStack(second));
   check(tile.resize(size),"test viewport resize");
   InventoryPlayer inventory=new InventoryPlayer(null); ReactorContainer container=new ReactorContainer(inventory,tile);
   boolean[] seen=new boolean[PocketLayout.CELLS];
   for(int y=0;y<size;y++) {
    container.setLayer(y); int visible=0;
    for(int page=0;page<ReactorContainer.PAGE_CELLS;page++) {
     Slot slot=(Slot)container.inventorySlots.get(page); int i=container.cellForPage(page); if(slot.yDisplayPosition<0) continue;
     check(PocketLayout.y(i)==y,"wrong layer visible");
     check(slot.xDisplayPosition>=12 && slot.xDisplayPosition+16<=174 && slot.yDisplayPosition>=36 && slot.yDisplayPosition+16<=36+size*18,"editor cell outside expanded grid");
     seen[i]=true; visible++;
    }
    check(visible==size*size,"entire layer must be visible at once");
   }
   for(int i=0;i<PocketLayout.CELLS;i++) check(seen[i]==PocketLayout.contains(i,size),"unreachable construction cell");
   int expectedHeight=size==5?234:size==7?270:306;
   check(ReactorScreenLayout.height(size)==expectedHeight,"wrong expanded window height");
   for(Object object:container.inventorySlots) {
    Slot slot=(Slot)object; if(slot.yDisplayPosition<0) continue;
    check(slot.xDisplayPosition>=0 && slot.xDisplayPosition+16<318 && slot.yDisplayPosition+16<expectedHeight,"slot outside expanded window");
   }
   Slot inventoryStart=(Slot)container.inventorySlots.get(ReactorContainer.PLAYER_START);
   check(inventoryStart.yDisplayPosition>36+size*18+18,"inventory overlaps full layer or action buttons");
   check(((Slot)container.inventorySlots.get(ReactorContainer.MODULE_1_SLOT)).yDisplayPosition==inventoryStart.yDisplayPosition+18,"module slots not moved with inventory");
   container.setLayer(size-2);
   inventory.setInventorySlotContents(9,new ItemStack(Items.water_bucket));
   check(container.transferStackInSlot(null,ReactorContainer.PLAYER_START)!=null,"liquid shift transfer into expanded layer");
   for(int i=0;i<PocketLayout.CELLS;i++) if(tile.getStackInSlot(i)!=null)
    check(PocketLayout.y(i)==size-2 && PocketLayout.accepts(i,PocketLayout.INTERIOR,size),"shift transfer used another layer or shell cell");
   verifyScale(size);
  }
  ReactorTile client=new ReactorTile(); ReactorContainer screen=new ReactorContainer(new InventoryPlayer(null),client);
  screen.updateProgressBar(18,9); screen.updateProgressBar(19,0); screen.setLayer(8);
  check(client.size==9 && screen.layer==8,"server size did not update client layout");
  check(screen.slotForCell(PocketLayout.slot(8,8,8)).yDisplayPosition==180,"last row hidden after server resize");
  screen.updateProgressBar(18,5); screen.updateProgressBar(19,0);
  check(screen.layer==4 && ((Slot)screen.inventorySlots.get(ReactorContainer.PLAYER_START)).yDisplayPosition==152,"shrinking did not restore compact layout");
 }
 private static void verifyScale(int size) {
  for(int[] resolution:new int[][]{{320,240},{340,260},{427,240},{640,360},{960,540}}) {
   int physicalW=resolution[0],physicalH=resolution[1];
   float scale=ReactorScreenLayout.fitScale(physicalW,physicalH,size);
   int w=ReactorScreenLayout.canvasExtent(physicalW,scale),h=ReactorScreenLayout.canvasExtent(physicalH,scale);
   int windowHeight=ReactorScreenLayout.height(size),left=(w-318)/2,top=(h-windowHeight)/2;
   check(scale>0 && scale<=1 && left>=0 && top>=0,"window does not fit small screen");
   check((left+318)*(physicalW/(float)w)<=physicalW && (top+windowHeight)*(physicalH/(float)h)<=physicalH,"scaled window extends beyond display");
   if(physicalW>=320 && physicalH>windowHeight+2) check(scale==1,"window shrunk despite enough space");
   // Vanilla mouse input uses the virtual canvas. Project each slot centre and map it back.
   for(int z=0;z<size;z++) for(int x=0;x<size;x++) {
    int slotX=left+12+(9-size)*9+x*18,slotY=top+36+z*18;
    int mouseX=Math.round((slotX+8)*physicalW/(float)w),mouseY=Math.round((slotY+8)*physicalH/(float)h);
    int virtualX=mouseX*w/physicalW,virtualY=mouseY*h/physicalH;
    check(virtualX>=slotX && virtualX<slotX+16 && virtualY>=slotY && virtualY<slotY+16,"scaled pointer misses cell centre");
   }
  }
 }
}
